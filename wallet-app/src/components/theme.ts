import { useColorScheme } from 'react-native';

const light = {
  background: '#F4F6F5',
  surface: '#FFFFFF',
  text: '#13201F',
  textMuted: '#5D6B69',
  border: '#DDE3E1',
  primary: '#0F6E62',
  onPrimary: '#FFFFFF',
  hero: '#0F3D3E',
  onHero: '#FFFFFF',
  onHeroMuted: '#A9C7C3',
  positive: '#16794C',
  negative: '#B3261E',
  dangerSurface: '#FCEDEC',
  inputBackground: '#FFFFFF',
};

const dark: typeof light = {
  background: '#0B1211',
  surface: '#151F1E',
  text: '#E7EFED',
  textMuted: '#93A39F',
  border: '#26332F',
  primary: '#4FC1AE',
  onPrimary: '#06201C',
  hero: '#123F3C',
  onHero: '#FFFFFF',
  onHeroMuted: '#9CC2BC',
  positive: '#5CD394',
  negative: '#FF8A80',
  dangerSurface: '#3A1714',
  inputBackground: '#101918',
};

export type Theme = typeof light;

export function useTheme(): Theme {
  return useColorScheme() === 'dark' ? dark : light;
}
