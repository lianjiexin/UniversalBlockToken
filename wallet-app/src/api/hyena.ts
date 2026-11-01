import {
  Account,
  BaseResponse,
  ListPointLogParams,
  ListResponse,
  ObjectResponse,
  PointLog,
  PointOpParams,
  ResCode,
} from './types';

/** The server answered, but with a non-zero Hyena status code. */
export class ApiError extends Error {
  constructor(
    public readonly code: number,
    public readonly serverMessage: string,
  ) {
    super(friendlyMessage(code, serverMessage));
    this.name = 'ApiError';
  }
}

/** The request never got a valid answer (offline, timeout, CORS, bad URL, non-JSON reply). */
export class NetworkError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'NetworkError';
  }
}

function friendlyMessage(code: number, serverMessage: string): string {
  switch (code) {
    case ResCode.NO_ENOUGH_POINT:
      return 'Insufficient balance.';
    case ResCode.PARAMETER_ERROR:
      return `Invalid request${serverMessage ? `: ${serverMessage}` : '.'}`;
    case ResCode.STATUS_ERROR:
      return 'This account is disabled or in the wrong state.';
    case ResCode.SERVICE_BUSY:
      return 'The server is busy. Please try again.';
    case ResCode.DUPLICATE:
    case ResCode.DUPLICATE_IDEMPOTENT:
      return 'This request is already being processed.';
    case ResCode.SERVER_ERROR:
    case ResCode.UNKNOWN_ERROR:
      return `The server hit an internal error (code ${code}). Check the server log.`;
    default:
      return serverMessage || `Server error (code ${code}).`;
  }
}

export interface HyenaClientOptions {
  baseUrl: string;
  timeoutMs?: number;
  fetchImpl?: typeof fetch;
}

/** Thin client for the Hyena REST API (/hyena/point/* and /hyena/system/*). */
export class HyenaClient {
  private readonly baseUrl: string;
  private readonly timeoutMs: number;
  private readonly fetchImpl: typeof fetch;

  constructor(opts: HyenaClientOptions) {
    this.baseUrl = normalizeBaseUrl(opts.baseUrl);
    this.timeoutMs = opts.timeoutMs ?? 15000;
    // Wrap the global so it is never invoked with the client as `this`
    // (browsers throw "Illegal invocation" for a detached window.fetch).
    this.fetchImpl = opts.fetchImpl ?? ((input, init) => fetch(input, init));
  }

  /** Returns null when the user has no account of this point type yet. */
  async getAccount(type: string, uid: string): Promise<Account | null> {
    // subUid must be sent as "" (not omitted). Hyena's write endpoints store a
    // missing subUid as "", but getPoint without one uses a different in-memory
    // cache key and keeps returning the balance from before later writes.
    const q = `type=${encodeURIComponent(type)}&uid=${encodeURIComponent(uid)}&subUid=`;
    const res = await this.request<ObjectResponse<Account>>('GET', `/hyena/point/getPoint?${q}`);
    return res.data ?? null;
  }

  async listLogs(params: ListPointLogParams): Promise<{ items: PointLog[]; total: number }> {
    const res = await this.request<ListResponse<PointLog>>('POST', '/hyena/point/listPointLog', params);
    return { items: res.data ?? [], total: res.total ?? 0 };
  }

  async listPointTypes(): Promise<string[]> {
    const res = await this.request<ListResponse<string>>('GET', '/hyena/system/listPointType');
    return res.data ?? [];
  }

  async increase(params: PointOpParams): Promise<Account> {
    const res = await this.request<ObjectResponse<Account>>('POST', '/hyena/point/increase', params);
    return requireData(res);
  }

  async decrease(params: PointOpParams): Promise<Account> {
    const res = await this.request<ObjectResponse<Account>>('POST', '/hyena/point/decrease', params);
    return requireData(res);
  }

  private async request<T extends BaseResponse>(method: 'GET' | 'POST', path: string, body?: unknown): Promise<T> {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), this.timeoutMs);
    let response: Response;
    try {
      response = await this.fetchImpl(this.baseUrl + path, {
        method,
        headers: body === undefined ? undefined : { 'Content-Type': 'application/json;charset=utf-8' },
        body: body === undefined ? undefined : JSON.stringify(body),
        signal: controller.signal,
      });
    } catch (e) {
      const aborted = (e as Error)?.name === 'AbortError';
      throw new NetworkError(
        aborted ? 'The server did not respond in time.' : `Cannot reach the server at ${this.baseUrl}.`,
      );
    } finally {
      clearTimeout(timer);
    }

    let json: T;
    try {
      json = (await response.json()) as T;
    } catch {
      throw new NetworkError(`Unexpected reply from the server (HTTP ${response.status}).`);
    }
    if (typeof json?.status !== 'number') {
      throw new NetworkError(`Unexpected reply from the server (HTTP ${response.status}).`);
    }
    if (json.status !== ResCode.SUCCESS) {
      throw new ApiError(json.status, json.error ?? '');
    }
    return json;
  }
}

function requireData<T>(res: ObjectResponse<T>): T {
  if (res.data == null) {
    throw new NetworkError('The server returned an empty result.');
  }
  return res.data;
}

export function normalizeBaseUrl(url: string): string {
  let u = url.trim().replace(/\/+$/, '');
  if (u && !/^https?:\/\//i.test(u)) {
    u = `http://${u}`;
  }
  return u;
}
