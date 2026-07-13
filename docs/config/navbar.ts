import type * as Preset from '@docusaurus/preset-classic';

// Shape of `themeConfig.navbar` — re-typed locally for clarity.
type Navbar = NonNullable<Preset.ThemeConfig['navbar']>;

export const navbar: Navbar = {
  logo: {
    alt: 'Axelix logo',
    src: 'img/logo.svg',
    srcDark: 'img/logo-dark.svg',
    href: '/product/introduction',
  },
  items: [
    {
      to: '/product/introduction',
      label: 'Product',
      position: 'left',
      activeBaseRegex: '^/product',
      className: 'navbar__tab',
    },
    {
      to: '/ui-guide/dashboard',
      label: 'UI Guide',
      position: 'left',
      activeBaseRegex: '^/ui-guide',
      className: 'navbar__tab',
    },
    {
      to: '/features/details',
      label: 'Features',
      position: 'left',
      activeBaseRegex: '^/features',
      className: 'navbar__tab',
    },
    {
      to: '/installation/configuring-master',
      label: 'Installation',
      position: 'left',
      activeBaseRegex: '^/installation',
      className: 'navbar__tab',
    },
    {
      to: '/setting-up-master-ui/what-is-master',
      label: 'Master',
      position: 'left',
      activeBaseRegex: '^/setting-up-master-ui',
      className: 'navbar__tab',
    },
    {
      to: '/setting-up-spring-boot-service/what-is-axelix-starter',
      label: 'Spring Boot Starter',
      position: 'left',
      activeBaseRegex: '^/setting-up-spring-boot-service',
      className: 'navbar__tab',
    },
    {
      to: '/more/glossary',
      label: 'More',
      position: 'left',
      activeBaseRegex: '^/more',
      className: 'navbar__tab',
    },
    {
      href: 'https://axelix.io/blog',
      label: 'Blog',
      position: 'right',
      className: 'navbar__pill',
    },
    {
      type: 'localeDropdown',
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
