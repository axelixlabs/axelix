import { themes as prismThemes } from 'prism-react-renderer';
import type * as Preset from '@docusaurus/preset-classic';
import { navbar } from './navbar';

export const themeConfig: Preset.ThemeConfig = {
  // Replace with your project's social card
  image: 'img/logo.svg',
  colorMode: {
    respectPrefersColorScheme: true,
  },
  navbar: navbar,
  prism: {
    theme: prismThemes.github,
    darkTheme: prismThemes.vsDark,
    additionalLanguages: ['java'],
  },
};
