// Order of docs in the Spring Boot Starter sidebar. Leading underscore makes
// Docusaurus ignore this file when scanning the docs content tree.
const prefix = 'setting-up-spring-boot-service';

const pages: string[] = [
  'what-is-axelix-starter',
  'configuring-axelix-starter',
].map((id) => `${prefix}/${id}`);

export default pages;
