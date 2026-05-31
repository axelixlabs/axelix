// Order of docs in the More sidebar. Leading underscore makes Docusaurus
// ignore this file when scanning the docs content tree.
const prefix = 'more';

const pages: string[] = [
  'glossary',
  'why-not-spring-boot-admin',
  'compatibility-matrix',
].map((id) => `${prefix}/${id}`);

export default pages;
