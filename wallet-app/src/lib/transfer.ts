import { ApiError, HyenaClient, NetworkError } from '@/api/hyena';
import { Tags } from './activity';

/**
 * Wallet-to-wallet transfer built from two Hyena calls: debit the sender,
 * then credit the recipient.
 *
 * Hyena has no atomic transfer endpoint, so this is a client-side saga:
 *  - Every call carries a `seq` derived from one transfer id. Hyena caches the
 *    response per seq (when `hyena.idempotent` is enabled), so retrying a call
 *    after a network failure cannot apply it twice.
 *  - If the server refuses the credit, the sender is re-credited. If the credit's
 *    outcome is unknown (no reply after retries), nothing is reversed, because
 *    the credit may have been applied; the transfer is reported as unresolved.
 *
 * For production this logic belongs on the server, behind authentication,
 * where it can run in one database transaction. See wallet-app/README.md.
 */

export interface TransferRequest {
  type: string;
  from: string;
  to: string;
  /** Normalized decimal string, see parseAmount(). */
  amount: string;
  memo?: string;
}

export type TransferFailure =
  /** Nothing was moved: the server refused the debit. */
  | 'not_sent'
  /** The debit got no reply after retries; it may or may not have been applied. */
  | 'unconfirmed'
  /** Debit succeeded, credit failed, and the sender was refunded. */
  | 'reversed'
  /** Debit succeeded, but neither credit nor refund could be confirmed. Needs manual review. */
  | 'unresolved';

export class TransferError extends Error {
  constructor(
    public readonly kind: TransferFailure,
    public readonly transferId: string,
    public readonly cause: unknown,
  ) {
    super(transferMessage(kind, transferId, cause));
    this.name = 'TransferError';
  }
}

function transferMessage(kind: TransferFailure, id: string, cause: unknown): string {
  const reason = cause instanceof Error ? cause.message : String(cause);
  switch (kind) {
    case 'not_sent':
      return reason;
    case 'unconfirmed':
      return `Could not confirm the transfer (${reason}) Check your activity before trying again. Reference ${id}.`;
    case 'reversed':
      return `The recipient could not be credited, so the amount was returned to you. (${reason})`;
    case 'unresolved':
      return `Your balance was debited, but the credit to the recipient could not be confirmed. Contact support with reference ${id}.`;
  }
}

export function newTransferId(now: number = Date.now(), rand: () => number = Math.random): string {
  const r = Math.floor(rand() * 36 ** 6)
    .toString(36)
    .padStart(6, '0');
  return `tx${now.toString(36)}${r}`;
}

async function withRetry<T>(fn: () => Promise<T>, attempts: number, delayMs: number): Promise<T> {
  let last: unknown;
  for (let i = 0; i < attempts; i++) {
    try {
      return await fn();
    } catch (e) {
      last = e;
      // Only transport failures are worth retrying; a business error will repeat.
      if (!(e instanceof NetworkError) || i === attempts - 1) break;
      await new Promise((r) => setTimeout(r, delayMs * 2 ** i));
    }
  }
  throw last;
}

export interface TransferOptions {
  transferId?: string;
  attempts?: number;
  retryDelayMs?: number;
}

export async function transfer(
  client: HyenaClient,
  req: TransferRequest,
  opts: TransferOptions = {},
): Promise<{ transferId: string }> {
  const id = opts.transferId ?? newTransferId();
  const attempts = opts.attempts ?? 3;
  const delay = opts.retryDelayMs ?? 500;
  const memo = req.memo?.trim();
  const withMemo = (s: string) => (memo ? `${s} · ${memo}` : s);

  try {
    await withRetry(
      () =>
        client.decrease({
          seq: `${id}-out`,
          type: req.type,
          uid: req.from,
          point: req.amount,
          tag: Tags.TRANSFER_OUT,
          orderNo: id,
          note: withMemo(`To ${req.to}`),
        }),
      attempts,
      delay,
    );
  } catch (e) {
    throw new TransferError(e instanceof ApiError ? 'not_sent' : 'unconfirmed', id, e);
  }

  try {
    await withRetry(
      () =>
        client.increase({
          seq: `${id}-in`,
          type: req.type,
          uid: req.to,
          point: req.amount,
          tag: Tags.TRANSFER_IN,
          orderNo: id,
          note: withMemo(`From ${req.from}`),
        }),
      attempts,
      delay,
    );
    return { transferId: id };
  } catch (creditError) {
    if (!(creditError instanceof ApiError)) {
      throw new TransferError('unresolved', id, creditError);
    }
    try {
      await withRetry(
        () =>
          client.increase({
            seq: `${id}-revert`,
            type: req.type,
            uid: req.from,
            point: req.amount,
            tag: Tags.TRANSFER_REVERT,
            orderNo: id,
            note: `Returned: transfer to ${req.to} failed`,
          }),
        attempts,
        delay,
      );
    } catch {
      throw new TransferError('unresolved', id, creditError);
    }
    throw new TransferError('reversed', id, creditError);
  }
}
