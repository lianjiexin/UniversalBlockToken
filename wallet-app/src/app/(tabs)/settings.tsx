import Constants from 'expo-constants';
import { useState } from 'react';
import { StyleSheet, Switch, Text, View } from 'react-native';

import { useTheme } from '@/components/theme';
import { Banner, Button, Card, Field, Screen, SectionTitle } from '@/components/ui';
import { Tags } from '@/lib/activity';
import { parseAmount } from '@/lib/amount';
import { newTransferId } from '@/lib/transfer';
import { useSignedInWallet } from '@/state/wallet';

export default function Settings() {
  const t = useTheme();
  const { session, client, prefs, setPrefs, signOut, markChanged } = useSignedInWallet();
  const [topUp, setTopUp] = useState('100');
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState<{ tone: 'error' | 'success'; text: string } | null>(null);

  async function addTestFunds() {
    setMessage(null);
    const a = parseAmount(topUp);
    if (!a.ok) {
      setMessage({ tone: 'error', text: a.error });
      return;
    }
    setBusy(true);
    try {
      const account = await client.increase({
        seq: newTransferId(),
        type: session.type,
        uid: session.uid,
        point: a.value,
        tag: Tags.TOP_UP,
        note: 'Test funds',
      });
      markChanged(account);
      setMessage({ tone: 'success', text: `Added ${a.value} ${session.type}.` });
    } catch (e) {
      setMessage({ tone: 'error', text: (e as Error).message });
    } finally {
      setBusy(false);
    }
  }

  const row = (label: string, value: string) => (
    <View style={styles.row}>
      <Text style={{ color: t.textMuted }}>{label}</Text>
      <Text selectable style={{ color: t.text, flexShrink: 1, textAlign: 'right' }}>
        {value}
      </Text>
    </View>
  );

  return (
    <Screen>
      <Card style={{ gap: 12 }}>
        <SectionTitle>Account</SectionTitle>
        {row('User ID', session.uid)}
        {row('Point type', session.type)}
        {row('Server', session.baseUrl)}
        {row('App version', Constants.expoConfig?.version ?? '-')}
        <Button title="Sign out" variant="danger" onPress={signOut} />
      </Card>

      <Card style={{ gap: 12 }}>
        <View style={styles.row}>
          <View style={{ flex: 1 }}>
            <Text style={{ color: t.text, fontWeight: '600', fontSize: 16 }}>Developer tools</Text>
            <Text style={{ color: t.textMuted }}>Add test funds to this wallet.</Text>
          </View>
          <Switch
            value={prefs.devTools}
            onValueChange={(v) => setPrefs({ devTools: v })}
            accessibilityLabel="Developer tools"
          />
        </View>
        {prefs.devTools ? (
          <>
            <Banner tone="info">
              This calls the server’s increase endpoint directly, which creates balance from nothing. Only use it on a
              test server.
            </Banner>
            <Field label="Amount" value={topUp} onChangeText={setTopUp} keyboardType="numeric" />
            {message ? <Banner tone={message.tone}>{message.text}</Banner> : null}
            <Button title="Add test funds" onPress={addTestFunds} busy={busy} />
          </>
        ) : null}
      </Card>
    </Screen>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', gap: 12 },
});
