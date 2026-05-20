import type * as Preset from '@docusaurus/preset-classic';

// Options passed to the `classic` preset (docs + blog + theme).
export const presetOptions: Preset.Options = {
  docs: {
    sidebarPath: './sidebars.ts',
    // Please change this to your repo.
    // Remove this to remove the "edit this page" links.
  },
  blog: {
    showReadingTime: true,
    feedOptions: {
      type: ['rss', 'atom'],
      xslt: true,
    },
    // Useful options to enforce blogging best practices
    onInlineTags: 'warn',
    onInlineAuthors: 'warn',
    onUntruncatedBlogPosts: 'warn',
  },
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
