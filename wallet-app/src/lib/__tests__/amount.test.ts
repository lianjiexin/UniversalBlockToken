import { formatAmount, parseAmount } from '../amount';

describe('parseAmount', () => {
  it.each([
    ['10', '10'],
    ['010.50', '10.5'],
    ['0.01', '0.01'],
    ['1,000.00', '1000'],
    [' 7.1 ', '7.1'],
  ])('accepts %p as %p', (input, expected) => {
    expect(parseAmount(input)).toEqual({ ok: true, value: expected });
  });

  it.each(['', 'abc', '1.234', '-5', '1e3', '.5', '5.'])('rejects %p', (input) => {
    expect(parseAmount(input).ok).toBe(false);
  });

  it('rejects zero', () => {
    expect(parseAmount('0.00')).toEqual({ ok: false, error: 'Amount must be greater than zero.' });
  });

  it('checks the available balance without float error', () => {
    expect(parseAmount('0.3', 0.1 + 0.2).ok).toBe(true);
    expect(parseAmount('0.31', 0.3).ok).toBe(false);
    expect(parseAmount('100', 100).ok).toBe(true);
  });
});

describe('formatAmount', () => {
  it('formats with two decimals and grouping', () => {
    expect(formatAmount(1234.5)).toBe('1,234.50');
    expect(formatAmount(null)).toBe('0.00');
  });
});
