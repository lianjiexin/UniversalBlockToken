import * as Clipboard from 'expo-clipboard';
import * as Linking from 'expo-linking';
import { useState } from 'react';
import { Share, StyleSheet, Text, View } from 'react-native';
import QRCode from 'react-native-qrcode-svg';

import { useTheme } from '@/components/theme';
import { Button, Card, Screen } from '@/components/ui';
import { useSignedInWallet } from '@/state/wallet';

export default function Receive() {
  const t = useTheme();
  const { session } = useSignedInWallet();
  const [copied, setCopied] = useState(false);
  // Opens the Send tab with the recipient filled in, in this app on any platform.
  const payLink = Linking.createURL('/send', { queryParams: { to: session.uid } });

  async function copy() {
    await Clipboard.setStringAsync(session.uid);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  return (
    <Screen>
      <Card style={{ alignItems: 'center', gap: 16 }}>
        <Text style={{ color: t.textMuted }}>Your {session.type} wallet ID</Text>
        <Text selectable style={[styles.uid, { color: t.text }]}>
          {session.uid}
        </Text>
        <View style={styles.qr}>
          <QRCode value={payLink} size={220} backgroundColor="#FFFFFF" color="#000000" />
        </View>
        <Text style={{ color: t.textMuted, textAlign: 'center' }}>
          Others can send to you by entering this ID, or by opening the QR code link.
        </Text>
        <View style={{ alignSelf: 'stretch', gap: 12 }}>
          <Button title={copied ? 'Copied' : 'Copy ID'} onPress={copy} />
          <Button
            title="Share"
            variant="secondary"
            onPress={() =>
              // Web browsers without the Web Share API reject; Copy ID still works there.
              Share.share({ message: `Send me ${session.type} on UBT Wallet: ${session.uid}\n${payLink}` }).catch(
                () => copy(),
              )
            }
          />
        </View>
      </Card>
    </Screen>
  );
}

const styles = StyleSheet.create({
  uid: { fontSize: 22, fontWeight: '700', textAlign: 'center' },
  qr: { padding: 12, backgroundColor: '#FFFFFF', borderRadius: 12 },
});
