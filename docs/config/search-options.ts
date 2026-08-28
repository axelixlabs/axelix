// Options for @easyops-cn/docusaurus-search-local, which builds a search
// index at build time and serves the search bar entirely from static assets.
// Registered as a theme in docusaurus.config.ts.
// Typed as a plain record: the plugin doesn't expose its PluginOptions type
// through its package name, and Docusaurus' `themes` tuple expects an
// index-signature-bearing options object anyway.
export const searchOptions: Record<string, unknown> = {
  // Both site locales get their own lunr index (Russian needs lunr-languages,
  // which the plugin bundles).
  language: ['en', 'ru'],
  // Docs are served at the root of the /docs/ baseUrl (see preset-options.ts),
  // so point the indexer at '/' and skip the (disabled) blog.
  docsRouteBasePath: '/',
  indexBlog: false,
  // Content-hash the generated index so browsers pick up rebuilds.
  hashed: true,
  // Don't highlight matched terms on the destination page after navigating
  // to a result.
  highlightSearchTermsOnTargetPage: false,
  searchResultLimits: 8,
  searchResultContextMaxLength: 50,
};
