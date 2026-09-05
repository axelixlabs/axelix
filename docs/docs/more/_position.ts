// Order of docs in the More sidebar. Leading underscore makes Docusaurus
// ignore this file when scanning the docs content tree.
const prefix = 'more';

const pages = [
  'glossary',
  'why-not-spring-boot-admin',
  'compatibility-and-versioning',
  'troubleshooting',
].map((id) => `${prefix}/${id}`);

const sidebar = [
  ...pages,
  {
    type: 'category' as const,
    label: 'Development',
    collapsed: false,
    items: [`${prefix}/development/branching-model`],
  },
];

export default sidebar;
