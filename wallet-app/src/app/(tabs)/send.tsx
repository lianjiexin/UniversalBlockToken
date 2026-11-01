import { useLocalSearchParams } from 'expo-router';
import { useState } from 'react';
import { Platform, Text, View } from 'react-native';

import { useTheme } from '@/components/theme';
import { Banner, Button, Card, Field, Screen } from '@/components/ui';
import { formatAmount, parseAmount } from '@/lib/amount';
import { transfer, TransferError } from '@/lib/transfer';
import { useSignedInWallet } from '@/state/wallet';

type Step = 'edit' | 'confirm';

export default function Send() {
  const t = useTheme();
  const { session, client, account, markChanged } = useSignedInWallet();
  // Allows deep links such as ubtwallet://send?to=user_456 (and /send?to=… on web).
  const params = useLocalSearchParams<{ to?: string; amount?: string }>();

  const [to, setTo] = useState(params.to ?? '');
  const [amount, setAmount] = useState(params.amount ?? '');
  const [memo, setMemo] = useState('');
  const [step, setStep] = useState<Step>('edit');
  const [busy, setBusy] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<{ to?: string; amount?: string }>({});
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [checkedAmount, setCheckedAmount] = useState('');

  // The tab stays mounted, so apply new deep-link params when they change
  // (React's "adjusting state when a prop changes" pattern).
  const [seenParams, setSeenParams] = useState({ to: params.to, amount: params.amount });
  if (params.to !== seenParams.to || params.amount !== seenParams.amount) {
    setSeenParams({ to: params.to, amount: params.amount });
    if (params.to) setTo(params.to);
    if (params.amount) setAmount(params.amount);
    setStep('edit');
  }

  const available = account?.available ?? 0;

  async function review() {
    setError(null);
    setSuccess(null);
    const recipient = to.trim();
    const errs: { to?: string; amount?: string } = {};
    if (!recipient) errs.to = 'Enter the recipient’s user ID.';
    else if (recipient === session.uid) errs.to = 'You cannot send to your own wallet.';
    const a = parseAmount(amount, available);
    if (!a.ok) errs.amount = a.error;
    setFieldErrors(errs);
    if (errs.to || !a.ok) return;

    // Hyena creates an account on first credit, so a typo would silently create
    // a new wallet. Require that the recipient already exists.
    setBusy(true);
    try {
      const recipientAccount = await client.getAccount(session.type, recipient);
      if (!recipientAccount) {
        setFieldErrors({ to: `No ${session.type} wallet found for "${recipient}". Check the ID.` });
        return;
      }
      setCheckedAmount(a.value);
      setStep('confirm');
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  async function send() {
    setBusy(true);
    setError(null);
    try {
      const { transferId } = await transfer(client, {
        type: session.type,
        from: session.uid,
        to: to.trim(),
        amount: checkedAmount,
        memo,
      });
      setSuccess(`Sent ${formatAmount(Number(checkedAmount))} to ${to.trim()}. Reference ${transferId}.`);
      setTo('');
      setAmount('');
      setMemo('');
      setStep('edit');
    } catch (e) {
      setError(e instanceof TransferError ? e.message : (e as Error).message);
      if (e instanceof TransferError && e.kind !== 'not_sent') setStep('edit');
    } finally {
      setBusy(false);
      markChanged();
    }
  }

  if (step === 'confirm') {
    return (
      <Screen>
        <Card style={{ gap: 12 }}>
          <Text style={{ color: t.textMuted }}>You are sending</Text>
          <Text style={{ color: t.text, fontSize: 36, fontWeight: '700' }}>
            {formatAmount(Number(checkedAmount))} <Text style={{ fontSize: 18 }}>{session.type}</Text>
          </Text>
          <Text style={{ color: t.textMuted }}>to</Text>
          <Text style={{ color: t.text, fontSize: 20, fontWeight: '600' }}>{to.trim()}</Text>
          {memo.trim() ? <Text style={{ color: t.textMuted }}>Memo: {memo.trim()}</Text> : null}
          {error ? <Banner tone="error">{error}</Banner> : null}
          <Button title="Confirm and send" onPress={send} busy={busy} />
          <Button title="Back" variant="secondary" onPress={() => setStep('edit')} disabled={busy} />
        </Card>
      </Screen>
    );
  }

  return (
    <Screen>
      {success ? <Banner tone="success">{success}</Banner> : null}
      {error ? <Banner tone="error">{error}</Banner> : null}
      <Card style={{ gap: 16 }}>
        <Field
          label="Recipient user ID"
          value={to}
          onChangeText={setTo}
          placeholder="e.g. user_456"
          error={fieldErrors.to}
        />
        <Field
          label="Amount"
          value={amount}
          onChangeText={setAmount}
          placeholder="0.00"
          keyboardType={Platform.OS === 'ios' ? 'decimal-pad' : 'numeric'}
          error={fieldErrors.amount}
          hint={`Available: ${formatAmount(available)} ${session.type}`}
        />
        <Field label="Memo (optional)" value={memo} onChangeText={setMemo} maxLength={100} autoCapitalize="sentences" />
        <View>
          <Button title="Review" onPress={review} busy={busy} disabled={!account} />
        </View>
      </Card>
    </Screen>
  );
}
