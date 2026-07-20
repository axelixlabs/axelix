import type { Config } from '@docusaurus/types';

import { llmsTxtOptions } from './config/llms-txt-options';
import { presetOptions } from './config/preset-options';
import { themeConfig } from './config/theme-config';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const config: Config = {
  title: 'Axelix',

  favicon: 'img/favicon.svg',

  // Future flags, see https://docusaurus.io/docs/api/docusaurus-config#future
  future: {
    v4: true, // Improve compatibility with the upcoming Docusaurus v4
  },

  // Set the production url of your site here
  url: 'https://axelix.io',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  // The whole docs site is served under /docs/. Combined with the docs
  // routeBasePath of '/', Docusaurus inserts the locale segment *after*
  // /docs/, so non-default locales live at /docs/<locale>/... (e.g.
  // /docs/ru/...) instead of the default /<locale>/docs/... layout.
  baseUrl: '/docs/',

  onBrokenLinks: 'throw',

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

  plugins: [['@signalwire/docusaurus-plugin-llms-txt', llmsTxtOptions]],

  themeConfig: themeConfig,
};

export default config;
