// Order of docs in the Master sidebar. Leading underscore makes Docusaurus
// ignore this file when scanning the docs content tree.
const prefix = 'setting-up-master-ui';

const pages: string[] = [
  'what-is-master',
  'configuring-master/configuring-master',
  'authentication/authentication',
  'mcp/mcp-tools',
].map((id) => `${prefix}/${id}`);

export default pages;
