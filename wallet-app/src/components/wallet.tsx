import { StyleSheet, Text, View } from 'react-native';

import { Account, PointLog } from '@/api/types';
import { describeLog } from '@/lib/activity';
import { formatAmount } from '@/lib/amount';
import { useTheme } from './theme';

export function BalanceCard({ account, type, uid }: { account: Account | null; type: string; uid: string }) {
  const t = useTheme();
  const stat = (label: string, value: number | undefined) => (
    <View style={styles.stat}>
      <Text style={[styles.statLabel, { color: t.onHeroMuted }]}>{label}</Text>
      <Text style={[styles.statValue, { color: t.onHero }]}>{formatAmount(value)}</Text>
    </View>
  );
  return (
    <View style={[styles.hero, { backgroundColor: t.hero }]}>
      <Text style={[styles.heroLabel, { color: t.onHeroMuted }]}>Available · {type}</Text>
      <Text style={[styles.heroAmount, { color: t.onHero }]} accessibilityLabel={`Available balance ${formatAmount(account?.available)}`}>
        {formatAmount(account?.available)}
      </Text>
      <Text style={[styles.heroUid, { color: t.onHeroMuted }]} numberOfLines={1}>
        {uid}
      </Text>
      <View style={styles.stats}>
        {stat('On hold', account?.frozen)}
        {stat('Spent', account?.used)}
        {stat('Expired', account?.expire)}
      </View>
    </View>
  );
}

export function ActivityRow({ log }: { log: PointLog }) {
  const t = useTheme();
  const v = describeLog(log);
  const sign = v.direction === 'in' ? '+' : v.direction === 'out' ? '−' : '';
  const color = v.direction === 'in' ? t.positive : v.direction === 'out' ? t.text : t.textMuted;
  return (
    <View style={[styles.row, { borderBottomColor: t.border }]}>
      <View style={{ flex: 1, gap: 2 }}>
        <Text style={[styles.rowTitle, { color: t.text }]}>{v.title}</Text>
        {v.subtitle ? (
          <Text style={{ color: t.textMuted }} numberOfLines={1}>
            {v.subtitle}
          </Text>
        ) : null}
        <Text style={{ color: t.textMuted, fontSize: 12 }}>{v.time}</Text>
      </View>
      <Text style={[styles.rowAmount, { color }]}>
        {sign}
        {formatAmount(v.amount)}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  hero: { borderRadius: 20, padding: 20, gap: 4 },
  heroLabel: { fontSize: 14, fontWeight: '600', textTransform: 'uppercase', letterSpacing: 0.5 },
  heroAmount: { fontSize: 40, fontWeight: '700', fontVariant: ['tabular-nums'] },
  heroUid: { fontSize: 14 },
  stats: { flexDirection: 'row', marginTop: 16, gap: 12 },
  stat: { flex: 1 },
  statLabel: { fontSize: 12 },
  statValue: { fontSize: 16, fontWeight: '600', fontVariant: ['tabular-nums'] },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    paddingVertical: 12,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  rowTitle: { fontSize: 16, fontWeight: '600' },
  rowAmount: { fontSize: 16, fontWeight: '700', fontVariant: ['tabular-nums'] },
});
