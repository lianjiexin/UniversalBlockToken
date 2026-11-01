import { useCallback, useEffect, useRef, useState } from 'react';
import { ActivityIndicator, FlatList, RefreshControl, StyleSheet, Text, View } from 'react-native';

import { PointLog } from '@/api/types';
import { useTheme } from '@/components/theme';
import { Banner } from '@/components/ui';
import { ActivityRow } from '@/components/wallet';
import { useSignedInWallet } from '@/state/wallet';

const PAGE_SIZE = 20;

export default function Activity() {
  const t = useTheme();
  const { session, client, revision } = useSignedInWallet();
  const [items, setItems] = useState<PointLog[]>([]);
  const [total, setTotal] = useState(0);
  const [firstLoadDone, setFirstLoadDone] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);
  // Bumped on every first-page load so a slow "load more" from before a reload is discarded.
  const generation = useRef(0);

  useEffect(() => {
    const gen = ++generation.current;
    client.listLogs({ type: session.type, uid: session.uid, start: 0, size: PAGE_SIZE }).then(
      (res) => {
        if (gen !== generation.current) return;
        setItems(res.items);
        setTotal(res.total);
        setError(null);
        setFirstLoadDone(true);
        setRefreshing(false);
      },
      (e: Error) => {
        if (gen !== generation.current) return;
        setError(e.message);
        setFirstLoadDone(true);
        setRefreshing(false);
      },
    );
  }, [client, session, revision, reloadKey]);

  const onRefresh = useCallback(() => {
    setRefreshing(true);
    setReloadKey((k) => k + 1);
  }, []);

  const loadMore = useCallback(async () => {
    if (loadingMore || refreshing || items.length >= total) return;
    const gen = generation.current;
    setLoadingMore(true);
    try {
      const res = await client.listLogs({ type: session.type, uid: session.uid, start: items.length, size: PAGE_SIZE });
      if (gen !== generation.current) return;
      setItems((cur) => {
        const seen = new Set(cur.map((i) => i.id));
        return [...cur, ...res.items.filter((i) => !seen.has(i.id))];
      });
      setTotal(res.total);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoadingMore(false);
    }
  }, [client, session, items.length, total, loadingMore, refreshing]);

  return (
    <FlatList
      style={{ backgroundColor: t.background }}
      contentContainerStyle={styles.content}
      data={items}
      keyExtractor={(i) => String(i.id)}
      renderItem={({ item }) => (
        <View style={styles.column}>
          <ActivityRow log={item} />
        </View>
      )}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
      onEndReached={loadMore}
      onEndReachedThreshold={0.4}
      ListHeaderComponent={
        error ? (
          <View style={[styles.column, { marginBottom: 12 }]}>
            <Banner tone="error">{error}</Banner>
          </View>
        ) : undefined
      }
      ListEmptyComponent={
        firstLoadDone && !error ? <Text style={[styles.empty, { color: t.textMuted }]}>No activity yet.</Text> : undefined
      }
      ListFooterComponent={
        loadingMore || !firstLoadDone ? <ActivityIndicator style={{ margin: 16 }} /> : undefined
      }
    />
  );
}

const styles = StyleSheet.create({
  content: { padding: 16 },
  // Center each row rather than the list content, which would shrink rows to fit their text.
  column: { width: '100%', maxWidth: 560, alignSelf: 'center' },
  empty: { textAlign: 'center', marginTop: 32 },
});
