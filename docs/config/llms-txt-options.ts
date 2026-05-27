import type { PluginOptions } from '@signalwire/docusaurus-plugin-llms-txt';
import type { Element, Root } from 'hast';
import { visit } from 'unist-util-visit';

// Drops inline data-URL images (e.g. Docusaurus' external-link icon, which is
// embedded as a base64 PNG) so they don't pollute the generated Markdown.
const stripDataUrlImages = () => (tree: Root) => {
  visit(tree, 'element', (node: Element, index, parent) => {
    if (
      node.tagName === 'img' &&
      typeof node.properties?.src === 'string' &&
      node.properties.src.startsWith('data:') &&
      parent &&
      typeof index === 'number'
    ) {
      parent.children.splice(index, 1);
      return ['skip', index];
    }
    return undefined;
  });
};

// Generates llms.txt and llms-full.txt alongside the HTML build so AI
// assistants can ingest the docs.
export const llmsTxtOptions: PluginOptions = {
  siteTitle: 'Axelix Documentation',
  siteDescription:
    'Axelix helps developers and QA engineers debug, test, and monitor Spring Boot Java/Kotlin applications.',
  content: {
    enableMarkdownFiles: true,
    enableLlmsFullTxt: true,
    includeDocs: true,
    includeBlog: false,
    includePages: false,
    relativePaths: false,
    enableMarkdownFiles: false,
    beforeDefaultRehypePlugins: [stripDataUrlImages],
  },
};
