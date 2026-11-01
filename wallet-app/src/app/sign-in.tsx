import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { HyenaClient } from '@/api/hyena';
import { useTheme } from '@/components/theme';
import { Banner, Button, Card, Field, Screen } from '@/components/ui';
import { DEFAULT_POINT_TYPE, DEFAULT_SERVER_URL, useWallet } from '@/state/wallet';

export default function SignIn() {
  const t = useTheme();
  const { signIn } = useWallet();
  const [baseUrl, setBaseUrl] = useState(DEFAULT_SERVER_URL);
  const [uid, setUid] = useState('');
  const [type, setType] = useState(DEFAULT_POINT_TYPE);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [knownTypes, setKnownTypes] = useState<string[]>([]);

  async function connect() {
    setError(null);
    if (!uid.trim()) {
      setError('Enter your user ID.');
      return;
    }
    setBusy(true);
    try {
      const client = new HyenaClient({ baseUrl });
      const types = await client.listPointTypes();
      setKnownTypes(types);
      const pointType = type.trim() || DEFAULT_POINT_TYPE;
      if (types.length === 0) {
        setError(
          'Connected, but this server has no point types yet. Create one first, for example: ' +
            `POST ${baseUrl.replace(/\/+$/, '')}/hyena/system/addPointType?name=${pointType}`,
        );
        return;
      }
      if (!types.includes(pointType)) {
        setError(`Point type "${pointType}" does not exist on this server. Pick one of the types below.`);
        return;
      }
      await signIn({ baseUrl, uid, type: pointType });
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: t.background }}>
      <Screen>
        <View style={styles.header}>
          <Text style={[styles.title, { color: t.text }]}>UBT Wallet</Text>
          <Text style={{ color: t.textMuted, fontSize: 16 }}>Open your balance on a Hyena point server.</Text>
        </View>
        <Card style={{ gap: 16 }}>
          <Field
            label="User ID"
            value={uid}
            onChangeText={setUid}
            placeholder="e.g. user_123"
            autoFocus
            returnKeyType="go"
            onSubmitEditing={connect}
          />
          <Field
            label="Server URL"
            value={baseUrl}
            onChangeText={setBaseUrl}
            keyboardType="url"
            hint="Address of the Hyena service."
          />
          <Field
            label="Point type"
            value={type}
            onChangeText={setType}
            hint="The currency or point balance to open."
          />
          {knownTypes.length > 0 ? (
            <View style={styles.chips}>
              {knownTypes.map((k) => (
                <Button key={k} title={k} variant="secondary" onPress={() => setType(k)} />
              ))}
            </View>
          ) : null}
          {error ? <Banner tone="error">{error}</Banner> : null}
          <Button title="Open wallet" onPress={connect} busy={busy} />
        </Card>
        <Text style={{ color: t.textMuted, fontSize: 13, lineHeight: 18 }}>
          The Hyena API has no user authentication. Anyone who can reach the server can open any user ID. Put an
          authenticating gateway in front of it before real use.
        </Text>
      </Screen>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  header: { gap: 6, marginTop: 24, marginBottom: 8 },
  title: { fontSize: 32, fontWeight: '800' },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
});
