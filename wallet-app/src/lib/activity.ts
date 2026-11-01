import { OpType, PointLog } from '@/api/types';

/** Tags this app writes on the logs it creates, so it can label them later. */
export const Tags = {
  TRANSFER_OUT: 'wallet_transfer_out',
  TRANSFER_IN: 'wallet_transfer_in',
  TRANSFER_REVERT: 'wallet_transfer_revert',
  TOP_UP: 'wallet_top_up',
} as const;

export type Direction = 'in' | 'out' | 'neutral';

export interface ActivityView {
  title: string;
  subtitle: string;
  /** Signed effect on the spendable balance. */
  direction: Direction;
  amount: number;
  time: string;
}

const TYPE_LABEL: Record<number, { title: string; direction: Direction }> = {
  [OpType.INCREASE]: { title: 'Received', direction: 'in' },
  [OpType.DECREASE]: { title: 'Spent', direction: 'out' },
  [OpType.FREEZE]: { title: 'On hold', direction: 'out' },
  [OpType.UNFREEZE]: { title: 'Hold released', direction: 'in' },
  [OpType.EXPIRE]: { title: 'Expired', direction: 'out' },
  [OpType.CANCEL]: { title: 'Cancelled', direction: 'out' },
  [OpType.REFUND]: { title: 'Refunded', direction: 'out' },
};

const TAG_TITLE: Record<string, string> = {
  [Tags.TRANSFER_OUT]: 'Sent',
  [Tags.TRANSFER_IN]: 'Received',
  [Tags.TRANSFER_REVERT]: 'Transfer reversed',
  [Tags.TOP_UP]: 'Test funds added',
};

export function describeLog(log: PointLog): ActivityView {
  const base = TYPE_LABEL[log.type] ?? { title: 'Adjustment', direction: 'neutral' as Direction };
  const title = (log.tag && TAG_TITLE[log.tag]) || base.title;
  const subtitle = log.note?.trim() || log.orderNo?.trim() || '';
  return {
    title,
    subtitle,
    direction: base.direction,
    amount: Number(log.delta) || 0,
    time: log.createTime ?? '',
  };
}
