import type { Config } from '@docusaurus/types';

import { presetOptions } from './config/preset-options';
import { themeConfig } from './config/theme-config';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const config: Config = {
  title: 'Axelix',

  // TODO: Fix this in future
  tagline: 'Dinosaurs are cool',
  favicon: 'img/favicon.svg',

  // Future flags, see https://docusaurus.io/docs/api/docusaurus-config#future
  future: {
    v4: true, // Improve compatibility with the upcoming Docusaurus v4
    faster: false, // Opt out of Rspack-based faster build — keeps @docusaurus/faster unneeded
  },

  // Set the production url of your site here
  url: 'https://axelix.io',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/',

  onBrokenLinks: 'throw',

  stylesheets: [
    {
      href: 'https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600&display=swap',
      rel: 'stylesheet',
    },
  ],

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en', 'ru'],
    localeConfigs: {
      en: { label: 'English' },
      ru: { label: 'Русский' },
    },
  },

  presets: [['classic', presetOptions]],

  themeConfig,
};

export default config;
