// Order of docs in the Product sidebar. Leading underscore makes Docusaurus
// ignore this file when scanning the docs content tree, so it's only seen by
// `sidebars.ts` which imports it.
const prefix = 'product';

const pages: string[] = [
  'introduction',
  'motivation',
  'architecture',
].map((id) => `${prefix}/${id}`);

export default pages;
