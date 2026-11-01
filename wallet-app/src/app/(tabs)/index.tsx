import { router } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { RefreshControl, StyleSheet, Text, View } from 'react-native';

import { PointLog } from '@/api/types';
import { useTheme } from '@/components/theme';
import { Banner, Button, Card, Screen, SectionTitle } from '@/components/ui';
import { ActivityRow, BalanceCard } from '@/components/wallet';
import { useSignedInWallet } from '@/state/wallet';

const RECENT_COUNT = 5;

export default function Home() {
  const t = useTheme();
  const { session, client, account, accountError, accountLoaded, loading, refresh, revision } = useSignedInWallet();
  const [recent, setRecent] = useState<PointLog[]>([]);
  const [recentError, setRecentError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let active = true;
    client.listLogs({ type: session.type, uid: session.uid, start: 0, size: RECENT_COUNT }).then(
      ({ items }) => {
        if (!active) return;
        setRecent(items);
        setRecentError(null);
      },
      (e: Error) => active && setRecentError(e.message),
    );
    return () => {
      active = false;
    };
  }, [client, session, revision, reloadKey]);

  const onRefresh = useCallback(() => {
    refresh();
    setReloadKey((k) => k + 1);
  }, [refresh]);

  return (
    <Screen refreshControl={<RefreshControl refreshing={loading} onRefresh={onRefresh} />}>
      {accountError ? <Banner tone="error">{accountError}</Banner> : null}
      <BalanceCard account={account} type={session.type} uid={session.uid} />
      {accountLoaded && !account && !accountError ? (
        <Banner tone="info">This wallet has no balance yet. Share your user ID from the Receive tab to get funds.</Banner>
      ) : null}
      <View style={styles.actions}>
        <View style={{ flex: 1 }}>
          <Button title="Send" onPress={() => router.navigate('/send')} disabled={!account} />
        </View>
        <View style={{ flex: 1 }}>
          <Button title="Receive" variant="secondary" onPress={() => router.navigate('/receive')} />
        </View>
      </View>
      <Card>
        <SectionTitle right={<Button title="See all" variant="secondary" onPress={() => router.navigate('/activity')} />}>
          Recent activity
        </SectionTitle>
        {recentError ? <Banner tone="error">{recentError}</Banner> : null}
        {recent.length === 0 && !recentError ? (
          <Text style={{ color: t.textMuted, paddingVertical: 16 }}>No activity yet.</Text>
        ) : (
          recent.map((log) => <ActivityRow key={log.id} log={log} />)
        )}
      </Card>
    </Screen>
  );
}

const styles = StyleSheet.create({
  actions: { flexDirection: 'row', gap: 12 },
});
