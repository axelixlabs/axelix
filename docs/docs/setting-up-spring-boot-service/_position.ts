// Order of docs in the Spring Boot Service sidebar. Leading underscore makes
// Docusaurus ignore this file when scanning the docs content tree.
import type { SidebarsConfig } from '@docusaurus/plugin-content-docs';

const prefix = 'setting-up-spring-boot-service';
const withPrefix = (ids: string[]): string[] => ids.map((id) => `${prefix}/${id}`);

const pages: SidebarsConfig[string] = [
  {
    type: 'category',
    label: 'Spring Boot Starter',
    collapsed: false,
    items: withPrefix(['spring-boot-starter/what-is-spring-boot-starter', 'spring-boot-starter/configuration']),
  },
  {
    type: 'category',
    label: 'Build Plugin',
    collapsed: false,
    items: withPrefix(['build-plugin/what-is-build-plugin', 'build-plugin/configuration']),
  },
];

export default pages;
