import type * as Preset from '@docusaurus/preset-classic';

// Options passed to the `classic` preset (docs + blog + theme).
export const presetOptions: Preset.Options = {
  docs: {
    sidebarPath: './sidebars.ts',
    // Docs sit at the root of the /docs/ baseUrl so URLs stay /docs/...
    // (and /docs/<locale>/... for non-default locales).
    routeBasePath: '/',
    // Please change this to your repo.
    // Remove this to remove the "edit this page" links.

    editUrl: 'https://github.com/axelixlabs/axelix/edit/master/docs/',
  },
  blog: false,
  theme: {
    customCss: [
      './src/css/fonts.css',
      './src/css/tokens.css',
      './src/css/base.css',
      './src/css/navbar.css',
      './src/css/sidebar.css',
      './src/css/content.css',
      './src/css/chrome.css',
    ],
  },
};
