import AsyncStorage from '@react-native-async-storage/async-storage';
import { createContext, ReactNode, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { Platform } from 'react-native';

import { HyenaClient, normalizeBaseUrl } from '@/api/hyena';
import { Account } from '@/api/types';

export interface Session {
  baseUrl: string;
  uid: string;
  /** Hyena point type, e.g. "default" or "score". */
  type: string;
}

interface Loaded {
  key: string;
  account: Account | null;
  error: string | null;
}

interface Prefs {
  /** Shows the "add test funds" tool. Off by default; see README. */
  devTools: boolean;
}

interface WalletState {
  ready: boolean;
  session: Session | null;
  client: HyenaClient | null;
  account: Account | null;
  accountError: string | null;
  /** False until the first balance request for the current session has finished. */
  accountLoaded: boolean;
  /** True during a user-initiated refresh. */
  loading: boolean;
  /** Bumped after every balance change so lists know to reload. */
  revision: number;
  prefs: Prefs;
  signIn(session: Session): Promise<void>;
  signOut(): Promise<void>;
  refresh(): Promise<void>;
  /** Call after a write; updates the balance and bumps `revision`. */
  markChanged(account?: Account): void;
  setPrefs(p: Partial<Prefs>): Promise<void>;
}

const SESSION_KEY = 'ubt-wallet/session';
const PREFS_KEY = 'ubt-wallet/prefs';

export const DEFAULT_SERVER_URL =
  process.env.EXPO_PUBLIC_HYENA_URL ||
  // The Android emulator reaches the host machine at 10.0.2.2, not localhost.
  (Platform.OS === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080');

export const DEFAULT_POINT_TYPE = 'default';

const WalletContext = createContext<WalletState | null>(null);

async function readJson<T>(key: string): Promise<T | null> {
  try {
    const raw = await AsyncStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : null;
  } catch {
    return null;
  }
}

export function WalletProvider({ children }: { children: ReactNode }) {
  const [ready, setReady] = useState(false);
  const [session, setSession] = useState<Session | null>(null);
  // Account data is tagged with the session it was loaded for, so a previous
  // session's balance is never shown while the new one loads.
  const [loaded, setLoaded] = useState<Loaded | null>(null);
  const [loading, setLoading] = useState(false);
  const [revision, setRevision] = useState(0);
  const [prefs, setPrefsState] = useState<Prefs>({ devTools: false });

  useEffect(() => {
    (async () => {
      const [s, p] = await Promise.all([readJson<Session>(SESSION_KEY), readJson<Prefs>(PREFS_KEY)]);
      if (s?.uid && s.baseUrl) setSession(s);
      if (p) setPrefsState((cur) => ({ ...cur, ...p }));
      setReady(true);
    })();
  }, []);

  const client = useMemo(() => (session ? new HyenaClient({ baseUrl: session.baseUrl }) : null), [session]);
  const sessionKey = session ? `${session.baseUrl}|${session.type}|${session.uid}` : '';

  /** Fetches the balance without touching state, so effects and handlers can share it. */
  const fetchAccount = useCallback(async (): Promise<Loaded | null> => {
    if (!client || !session) return null;
    try {
      return { key: sessionKey, account: await client.getAccount(session.type, session.uid), error: null };
    } catch (e) {
      return { key: sessionKey, account: null, error: (e as Error).message };
    }
  }, [client, session, sessionKey]);

  // On a failed reload, keep showing the last known balance next to the error.
  const applyLoaded = useCallback((next: Loaded) => {
    setLoaded((cur) => (next.error && cur?.key === next.key ? { ...next, account: cur.account } : next));
  }, []);

  useEffect(() => {
    let active = true;
    fetchAccount().then((r) => {
      if (active && r) applyLoaded(r);
    });
    return () => {
      active = false;
    };
  }, [fetchAccount, applyLoaded]);

  const refresh = useCallback(async () => {
    setLoading(true);
    const r = await fetchAccount();
    if (r) applyLoaded(r);
    setLoading(false);
  }, [fetchAccount, applyLoaded]);

  const current = loaded?.key === sessionKey ? loaded : null;

  const signIn = useCallback(async (s: Session) => {
    const clean: Session = {
      baseUrl: normalizeBaseUrl(s.baseUrl),
      uid: s.uid.trim(),
      type: s.type.trim() || DEFAULT_POINT_TYPE,
    };
    await AsyncStorage.setItem(SESSION_KEY, JSON.stringify(clean));
    setSession(clean);
  }, []);

  const signOut = useCallback(async () => {
    await AsyncStorage.removeItem(SESSION_KEY);
    setSession(null);
    setLoaded(null);
  }, []);

  const markChanged = useCallback(
    (a?: Account) => {
      if (a) setLoaded({ key: sessionKey, account: a, error: null });
      else refresh();
      setRevision((r) => r + 1);
    },
    [refresh, sessionKey],
  );

  const setPrefs = useCallback(
    async (p: Partial<Prefs>) => {
      const next = { ...prefs, ...p };
      setPrefsState(next);
      await AsyncStorage.setItem(PREFS_KEY, JSON.stringify(next));
    },
    [prefs],
  );

  const value: WalletState = {
    ready,
    session,
    client,
    account: current?.account ?? null,
    accountError: current?.error ?? null,
    accountLoaded: current !== null,
    loading,
    revision,
    prefs,
    signIn,
    signOut,
    refresh,
    markChanged,
    setPrefs,
  };
  return <WalletContext.Provider value={value}>{children}</WalletContext.Provider>;
}

export function useWallet(): WalletState {
  const ctx = useContext(WalletContext);
  if (!ctx) throw new Error('useWallet must be used inside <WalletProvider>');
  return ctx;
}

/** Use inside the signed-in screens, where a session always exists. */
export function useSignedInWallet() {
  const w = useWallet();
  if (!w.session || !w.client) throw new Error('No active wallet session');
  return { ...w, session: w.session, client: w.client };
}
