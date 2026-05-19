// Order of docs in the Features sidebar. Leading underscore makes Docusaurus
// ignore this file when scanning the docs content tree.
const prefix = 'features';

const pages: string[] = [
  'details',
  'metrics',
  'loggers/loggers',
  'properties',
  'beans',
  'configuration-properties',
  'scheduled-tasks',
  'conditions',
  'caches',
  'transaction-control',
  'thread-dump',
  'garbage-collector',
].map((id) => `${prefix}/${id}`);

export default pages;
