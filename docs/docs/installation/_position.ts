// Order of docs in the Installation sidebar. Leading underscore makes Docusaurus
// ignore this file when scanning the docs content tree.
const prefix = 'installation';

const pages: string[] = [
  'configuring-master',
  'configuring-spring-boot-starter'
].map((id) => `${prefix}/${id}`);

export default pages;
