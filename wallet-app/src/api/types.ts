/**
 * Types mirroring the JSON returned by the Hyena point service
 * (see hyena-core/src/main/java/io/github/alphajiang/hyena/model).
 *
 * Java BigDecimal fields arrive as JSON numbers. Dates are strings in the
 * form "yyyy-MM-dd HH:mm:ss" (GMT+8, as configured on the server).
 */

/** Status codes from HyenaConstants.java. */
export const ResCode = {
  SUCCESS: 0,
  SERVICE_ERROR: 1000,
  NO_ENOUGH_POINT: 1010,
  PARAMETER_ERROR: 1100,
  STATUS_ERROR: 1200,
  SERVICE_BUSY: 1300,
  DUPLICATE: 2000,
  DUPLICATE_IDEMPOTENT: 2001,
  SERVER_ERROR: 9000,
  UNKNOWN_ERROR: 9999,
} as const;

export interface BaseResponse {
  status: number;
  error?: string;
  seq?: string;
}

export interface ObjectResponse<T> extends BaseResponse {
  data: T | null;
}

export interface ListResponse<T> extends BaseResponse {
  total: number;
  data: T[] | null;
}

/** A user's account for one point type (PointPo). */
export interface Account {
  id?: number;
  uid: string;
  subUid?: string | null;
  name?: string | null;
  /** Total valid points (available + frozen). */
  point: number;
  available: number;
  used: number;
  frozen: number;
  refund: number;
  expire: number;
  enable?: boolean;
  createTime?: string;
  updateTime?: string;
}

/** Operation types written into point logs (PointOpType.java). */
export const OpType = {
  INCREASE: 1,
  DECREASE: 2,
  FREEZE: 3,
  UNFREEZE: 4,
  EXPIRE: 5,
  CANCEL: 6,
  REFUND: 7,
} as const;

/** One balance change (PointLogDto). `delta` is always positive; `type` gives the direction. */
export interface PointLog {
  id: number;
  pid: number;
  uid: string;
  seqNum: number;
  delta: number;
  point: number;
  available: number;
  used: number;
  frozen: number;
  refund: number;
  expire: number;
  type: number;
  tag?: string | null;
  orderNo?: string | null;
  note?: string | null;
  createTime?: string;
}

/** Common body of the write endpoints (PointOpParam). */
export interface PointOpParams {
  /** Idempotency key: the server returns the cached response for a repeated seq. */
  seq?: string;
  type: string;
  uid: string;
  /** Sent as a string so the server's BigDecimal gets the exact decimal value. */
  point: string;
  tag?: string;
  orderNo?: string;
  note?: string;
}

export interface ListPointLogParams {
  type: string;
  uid: string;
  start?: number;
  size?: number;
}
