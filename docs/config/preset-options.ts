import type * as Preset from '@docusaurus/preset-classic';

// Options passed to the `classic` preset (docs + blog + theme).
export const presetOptions: Preset.Options = {
  docs: {
    sidebarPath: './sidebars.ts',
    // Please change this to your repo.
    // Remove this to remove the "edit this page" links.
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
