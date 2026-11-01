/** Amounts are entered and sent as decimal strings with at most this many fraction digits. */
export const MAX_DECIMALS = 2;

const AMOUNT_RE = new RegExp(`^\\d+(\\.\\d{1,${MAX_DECIMALS}})?$`);

export type AmountCheck = { ok: true; value: string } | { ok: false; error: string };

/**
 * Validates user input for an amount. Returns a normalized decimal string
 * (no leading zeros, no trailing dot) that is safe to send to the server.
 */
export function parseAmount(input: string, available?: number): AmountCheck {
  const raw = input.trim().replace(/,/g, '');
  if (raw === '') {
    return { ok: false, error: 'Enter an amount.' };
  }
  if (!AMOUNT_RE.test(raw)) {
    return { ok: false, error: `Use a number with up to ${MAX_DECIMALS} decimal places.` };
  }
  const [intPart, frac = ''] = raw.split('.');
  const whole = intPart.replace(/^0+(?=\d)/, '');
  const fraction = frac.replace(/0+$/, '');
  const normalized = fraction ? `${whole}.${fraction}` : whole;
  if (Number(normalized) <= 0) {
    return { ok: false, error: 'Amount must be greater than zero.' };
  }
  if (available !== undefined && toCents(normalized) > Math.round(available * 100)) {
    return { ok: false, error: 'Amount is more than your available balance.' };
  }
  return { ok: true, value: normalized };
}

/** Integer number of hundredths, avoiding floating point error in comparisons. */
function toCents(decimal: string): number {
  const [i, f = ''] = decimal.split('.');
  return Number(i) * 100 + Number((f + '00').slice(0, 2));
}

export function formatAmount(n: number | null | undefined): string {
  const v = typeof n === 'number' && Number.isFinite(n) ? n : 0;
  return v.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
