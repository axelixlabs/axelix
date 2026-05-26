// Order of docs in the Features sidebar. Leading underscore makes Docusaurus
// ignore this file when scanning the docs content tree.
import type { SidebarsConfig } from '@docusaurus/plugin-content-docs';

const prefix = 'features';
const withPrefix = (ids: string[]): string[] => ids.map((id) => `${prefix}/${id}`);

const pages: SidebarsConfig[string] = [
  {
    type: 'category',
    label: 'Insights',
    collapsed: false,
    items: withPrefix(['details', 'metrics', 'loggers/loggers']),
  },
  {
    type: 'category',
    label: 'Spring Framework',
    collapsed: false,
    items: withPrefix([
      'properties',
      'beans',
      'configuration-properties',
      'scheduled-tasks',
      'conditions',
      'caches',
      'transaction-control',
    ]),
  },
  {
    type: 'category',
    label: 'JVM',
    collapsed: false,
    items: withPrefix(['thread-dump', 'garbage-collector']),
  },
];

export default pages;
