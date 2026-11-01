import { ApiError, HyenaClient, NetworkError, normalizeBaseUrl } from '../hyena';

function jsonFetch(body: unknown, status = 200) {
  return jest.fn(async () => ({ status, json: async () => body }) as unknown as Response);
}

describe('HyenaClient', () => {
  it('calls getPoint with encoded query params and returns the account', async () => {
    const fetchImpl = jsonFetch({ status: 0, data: { uid: 'a b', available: 5 } });
    const c = new HyenaClient({ baseUrl: 'http://h:8080/', fetchImpl });
    const acct = await c.getAccount('default', 'a b');
    expect(acct?.available).toBe(5);
    expect(fetchImpl).toHaveBeenCalledWith(
      'http://h:8080/hyena/point/getPoint?type=default&uid=a%20b&subUid=',
      expect.objectContaining({ method: 'GET' }),
    );
  });

  it('returns null for a user without an account', async () => {
    const c = new HyenaClient({ baseUrl: 'http://h', fetchImpl: jsonFetch({ status: 0 }) });
    await expect(c.getAccount('default', 'nobody')).resolves.toBeNull();
  });

  it('posts JSON bodies to write endpoints', async () => {
    const fetchImpl = jsonFetch({ status: 0, data: { uid: 'u' } });
    const c = new HyenaClient({ baseUrl: 'http://h', fetchImpl });
    await c.decrease({ type: 'default', uid: 'u', point: '1.5', seq: 's' });
    const [url, init] = (fetchImpl.mock.calls[0] as unknown as [string, RequestInit]);
    expect(url).toBe('http://h/hyena/point/decrease');
    expect(JSON.parse(init.body as string)).toEqual({ type: 'default', uid: 'u', point: '1.5', seq: 's' });
  });

  it('turns a non-zero status into an ApiError', async () => {
    const c = new HyenaClient({ baseUrl: 'http://h', fetchImpl: jsonFetch({ status: 1010, error: '积分不足' }) });
    const err = await c.decrease({ type: 'default', uid: 'u', point: '1' }).catch((e) => e);
    expect(err).toBeInstanceOf(ApiError);
    expect(err.code).toBe(1010);
    expect(err.message).toBe('Insufficient balance.');
    expect(err.serverMessage).toBe('积分不足');
  });

  it('turns transport failures into a NetworkError', async () => {
    const fetchImpl = jest.fn(async () => {
      throw new TypeError('Failed to fetch');
    });
    const c = new HyenaClient({ baseUrl: 'http://h', fetchImpl });
    await expect(c.listPointTypes()).rejects.toBeInstanceOf(NetworkError);
  });

  it('rejects replies that are not Hyena responses', async () => {
    const c = new HyenaClient({ baseUrl: 'http://h', fetchImpl: jsonFetch({ hello: 'world' }, 404) });
    await expect(c.listPointTypes()).rejects.toThrow('HTTP 404');
  });
});

describe('default fetch', () => {
  it('calls the global fetch unbound from the client', async () => {
    const original = globalThis.fetch;
    const calls: unknown[] = [];
    globalThis.fetch = function (this: unknown) {
      // Browsers throw "Illegal invocation" when fetch's receiver is not the window.
      calls.push(this);
      return Promise.resolve({ status: 200, json: async () => ({ status: 0, data: [] }) } as unknown as Response);
    } as typeof fetch;
    try {
      const c = new HyenaClient({ baseUrl: 'http://h' });
      await c.listPointTypes();
      expect(calls).toHaveLength(1);
      expect(calls[0]).not.toBe(c);
    } finally {
      globalThis.fetch = original;
    }
  });
});

describe('normalizeBaseUrl', () => {
  it('adds a scheme and strips trailing slashes', () => {
    expect(normalizeBaseUrl(' 192.168.1.5:8080/ ')).toBe('http://192.168.1.5:8080');
    expect(normalizeBaseUrl('https://api.example.com//')).toBe('https://api.example.com');
  });
});
