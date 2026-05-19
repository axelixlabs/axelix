// Order of docs in the UI Guide sidebar. Leading underscore makes Docusaurus
// ignore this file when scanning the docs content tree.
const prefix = 'ui-guide';

const pages: string[] = [
  'dashboard',
  'wallboard',
  'service-profile',
].map((id) => `${prefix}/${id}`);

export default pages;
