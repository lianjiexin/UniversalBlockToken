import { ApiError, HyenaClient, NetworkError } from '@/api/hyena';
import { Account, PointOpParams, ResCode } from '@/api/types';
import { Tags } from '../activity';
import { transfer, TransferError } from '../transfer';

const account = { uid: 'x', point: 0, available: 0, used: 0, frozen: 0, refund: 0, expire: 0 } as Account;

type Op = 'increase' | 'decrease';

/** Fake client: each op pops the next scripted outcome, defaulting to success. */
function fakeClient(script: Partial<Record<Op, (Error | 'ok')[]>> = {}) {
  const calls: { op: Op; params: PointOpParams }[] = [];
  const run = (op: Op) => async (params: PointOpParams) => {
    calls.push({ op, params });
    const next = script[op]?.shift() ?? 'ok';
    if (next !== 'ok') throw next;
    return account;
  };
  const client = { increase: run('increase'), decrease: run('decrease') } as unknown as HyenaClient;
  return { client, calls };
}

const req = { type: 'default', from: 'alice', to: 'bob', amount: '12.5', memo: 'lunch' };
const opts = { transferId: 'tx1', retryDelayMs: 0 };

describe('transfer', () => {
  it('debits the sender then credits the recipient, with idempotency keys', async () => {
    const { client, calls } = fakeClient();
    await expect(transfer(client, req, opts)).resolves.toEqual({ transferId: 'tx1' });
    expect(calls).toEqual([
      {
        op: 'decrease',
        params: expect.objectContaining({ seq: 'tx1-out', uid: 'alice', point: '12.5', tag: Tags.TRANSFER_OUT, orderNo: 'tx1', note: 'To bob · lunch' }),
      },
      {
        op: 'increase',
        params: expect.objectContaining({ seq: 'tx1-in', uid: 'bob', point: '12.5', tag: Tags.TRANSFER_IN, orderNo: 'tx1', note: 'From alice · lunch' }),
      },
    ]);
  });

  it('does not credit when the debit is refused', async () => {
    const { client, calls } = fakeClient({ decrease: [new ApiError(ResCode.NO_ENOUGH_POINT, '')] });
    const err = await transfer(client, req, opts).catch((e) => e);
    expect(err).toBeInstanceOf(TransferError);
    expect(err.kind).toBe('not_sent');
    expect(err.message).toBe('Insufficient balance.');
    expect(calls.map((c) => c.op)).toEqual(['decrease']);
  });

  it('retries network failures with the same seq', async () => {
    const { client, calls } = fakeClient({ increase: [new NetworkError('down'), 'ok'] });
    await transfer(client, req, opts);
    expect(calls.map((c) => c.params.seq)).toEqual(['tx1-out', 'tx1-in', 'tx1-in']);
  });

  it('reports an unconfirmed debit when the server never answers', async () => {
    const down = new NetworkError('down');
    const { client, calls } = fakeClient({ decrease: [down, down, down] });
    const err = await transfer(client, req, opts).catch((e) => e);
    expect(err.kind).toBe('unconfirmed');
    expect(calls.map((c) => c.op)).toEqual(['decrease', 'decrease', 'decrease']);
  });

  it('refunds the sender when the server refuses the credit', async () => {
    const { client, calls } = fakeClient({ increase: [new ApiError(ResCode.STATUS_ERROR, 'disabled'), 'ok'] });
    const err = await transfer(client, req, opts).catch((e) => e);
    expect(err.kind).toBe('reversed');
    expect(calls[2]).toEqual({
      op: 'increase',
      params: expect.objectContaining({ seq: 'tx1-revert', uid: 'alice', point: '12.5', tag: Tags.TRANSFER_REVERT }),
    });
  });

  it('does not refund when the credit outcome is unknown', async () => {
    const down = new NetworkError('down');
    const { client, calls } = fakeClient({ increase: [down, down, down] });
    const err = await transfer(client, req, opts).catch((e) => e);
    expect(err.kind).toBe('unresolved');
    expect(calls.some((c) => c.params.seq === 'tx1-revert')).toBe(false);
  });

  it('reports unresolved when the refund also fails', async () => {
    const refused = new ApiError(ResCode.STATUS_ERROR, 'disabled');
    const { client } = fakeClient({ increase: [refused, refused] });
    const err = await transfer(client, req, opts).catch((e) => e);
    expect(err.kind).toBe('unresolved');
    expect(err.message).toContain('tx1');
  });
});
