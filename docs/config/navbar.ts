import type * as Preset from '@docusaurus/preset-classic';

// Shape of `themeConfig.navbar` — re-typed locally for clarity.
type Navbar = NonNullable<Preset.ThemeConfig['navbar']>;

export const navbar: Navbar = {
  logo: {
    alt: 'Axelix logo',
    src: 'img/logo.svg',
    srcDark: 'img/logo-dark.svg',
    href: '/docs/product/introduction',
  },
  items: [
    {
      to: '/docs/product/introduction',
      label: 'Product',
      position: 'left',
      activeBaseRegex: '^/docs/product',
      className: 'navbar__tab',
    },
    {
      to: '/docs/ui-guide/dashboard',
      label: 'UI Guide',
      position: 'left',
      activeBaseRegex: '^/docs/ui-guide',
      className: 'navbar__tab',
    },
    {
      to: '/docs/features/details',
      label: 'Features',
      position: 'left',
      activeBaseRegex: '^/docs/features',
      className: 'navbar__tab',
    },
    {
      to: '/docs/setting-up-master-ui/what-is-master',
      label: 'Master',
      position: 'left',
      activeBaseRegex: '^/docs/setting-up-master-ui',
      className: 'navbar__tab',
    },
    {
      to: '/docs/setting-up-spring-boot-service/what-is-axelix-starter',
      label: 'Spring Boot Starter',
      position: 'left',
      activeBaseRegex: '^/docs/setting-up-spring-boot-service',
      className: 'navbar__tab',
    },
    {
      to: '/docs/more/glossary',
      label: 'More',
      position: 'left',
      activeBaseRegex: '^/docs/more',
      className: 'navbar__tab',
    },
    {
      to: '/docs/product/introduction',
      label: 'Docs',
      position: 'right',
      className: 'navbar__pill',
    },
    {
      to: '/blog',
      label: 'Blog',
      position: 'right',
      className: 'navbar__pill',
    },
    {
      href: 'https://github.com/axelixlabs/axelix',
      label: 'GitHub',
      position: 'right',
      className: 'navbar__pill navbar__pill--primary header-github-link',
      htmlAttributes: {
        rel: 'noopener noreferrer',
      },
      'aria-label': 'GitHub repository',
    },
  ],
};
