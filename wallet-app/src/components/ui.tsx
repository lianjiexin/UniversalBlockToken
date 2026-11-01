import { ReactNode } from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  ScrollViewProps,
  StyleSheet,
  Text,
  TextInput,
  TextInputProps,
  View,
  ViewStyle,
} from 'react-native';

import { useTheme } from './theme';

/** Scrollable page body with a max width so the web layout stays readable on desktop. */
export function Screen({ children, ...rest }: { children: ReactNode } & ScrollViewProps) {
  const t = useTheme();
  return (
    <ScrollView
      style={{ flex: 1, backgroundColor: t.background }}
      contentContainerStyle={styles.screenContent}
      keyboardShouldPersistTaps="handled"
      {...rest}>
      <View style={styles.column}>{children}</View>
    </ScrollView>
  );
}

export function Card({ children, style }: { children: ReactNode; style?: ViewStyle }) {
  const t = useTheme();
  return (
    <View style={[styles.card, { backgroundColor: t.surface, borderColor: t.border }, style]}>{children}</View>
  );
}

export function Button({
  title,
  onPress,
  disabled,
  busy,
  variant = 'primary',
}: {
  title: string;
  onPress: () => void;
  disabled?: boolean;
  busy?: boolean;
  variant?: 'primary' | 'secondary' | 'danger';
}) {
  const t = useTheme();
  const bg = variant === 'primary' ? t.primary : 'transparent';
  const fg = variant === 'primary' ? t.onPrimary : variant === 'danger' ? t.negative : t.primary;
  const inactive = disabled || busy;
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ disabled: !!inactive, busy: !!busy }}
      onPress={onPress}
      disabled={inactive}
      style={({ pressed }) => [
        styles.button,
        { backgroundColor: bg, borderColor: variant === 'primary' ? bg : t.border },
        (pressed || inactive) && { opacity: inactive ? 0.5 : 0.8 },
      ]}>
      {busy ? <ActivityIndicator color={fg} /> : <Text style={[styles.buttonText, { color: fg }]}>{title}</Text>}
    </Pressable>
  );
}

export function Field({
  label,
  hint,
  error,
  ...input
}: { label: string; hint?: string; error?: string | null } & TextInputProps) {
  const t = useTheme();
  return (
    <View style={styles.field}>
      <Text style={[styles.label, { color: t.text }]}>{label}</Text>
      <TextInput
        placeholderTextColor={t.textMuted}
        autoCapitalize="none"
        autoCorrect={false}
        style={[
          styles.input,
          { color: t.text, backgroundColor: t.inputBackground, borderColor: error ? t.negative : t.border },
        ]}
        {...input}
      />
      {error ? (
        <Text style={[styles.hint, { color: t.negative }]}>{error}</Text>
      ) : hint ? (
        <Text style={[styles.hint, { color: t.textMuted }]}>{hint}</Text>
      ) : null}
    </View>
  );
}

export function Banner({ tone, children }: { tone: 'error' | 'success' | 'info'; children: ReactNode }) {
  const t = useTheme();
  const color = tone === 'error' ? t.negative : tone === 'success' ? t.positive : t.textMuted;
  return (
    <View
      accessibilityRole="alert"
      style={[
        styles.banner,
        { borderColor: color, backgroundColor: tone === 'error' ? t.dangerSurface : t.surface },
      ]}>
      <Text style={{ color: tone === 'error' ? t.negative : t.text, lineHeight: 20 }}>{children}</Text>
    </View>
  );
}

export function SectionTitle({ children, right }: { children: ReactNode; right?: ReactNode }) {
  const t = useTheme();
  return (
    <View style={styles.sectionRow}>
      <Text style={[styles.sectionTitle, { color: t.text }]}>{children}</Text>
      {right}
    </View>
  );
}

const styles = StyleSheet.create({
  screenContent: { padding: 16, paddingBottom: 40, alignItems: 'center' },
  column: { width: '100%', maxWidth: 560, gap: 16 },
  card: { borderRadius: 16, borderWidth: StyleSheet.hairlineWidth, padding: 16 },
  button: {
    minHeight: 48,
    borderRadius: 12,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 20,
  },
  buttonText: { fontSize: 16, fontWeight: '600' },
  field: { gap: 6 },
  label: { fontSize: 14, fontWeight: '600' },
  input: { minHeight: 48, borderWidth: 1, borderRadius: 12, paddingHorizontal: 14, fontSize: 16 },
  hint: { fontSize: 13 },
  banner: { borderWidth: 1, borderRadius: 12, padding: 12 },
  sectionRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  sectionTitle: { fontSize: 18, fontWeight: '700' },
});
