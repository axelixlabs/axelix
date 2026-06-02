import { access, mkdir, rename } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

// The @signalwire/docusaurus-plugin-llms-txt plugin always emits llms.txt /
// llms-full.txt at the build root (served from `/`), because it writes straight
// to Docusaurus' `outDir` with no output-path option. We serve docs under
// `/docs/` (the `docs` routeBasePath) and nginx only proxies `/docs/` (and
// `/ru/docs/`) to the docs container, so relocate the files into the docs
// namespace once the whole build is finished (the plugin runs in postBuild,
// which Docusaurus executes in parallel across plugins — doing this here, after
// `docusaurus build`, avoids racing the generation).

const buildDir = fileURLToPath(new URL('../build', import.meta.url));

// '' is the default locale (en) at the build root; non-default locales live in
// their own subdirectory. Keep in sync with i18n.locales in docusaurus.config.ts.
const locales = ['', 'ru'];
const files = ['llms.txt', 'llms-full.txt'];

for (const locale of locales) {
  const localeRoot = path.join(buildDir, locale);
  const destDir = path.join(localeRoot, 'docs');

  for (const file of files) {
    const src = path.join(localeRoot, file);

    try {
      await access(src);
    } catch {
      continue; // not generated for this locale — skip
    }

    await mkdir(destDir, { recursive: true });
    const dest = path.join(destDir, file);
    await rename(src, dest);
    console.log(
      `moved ${path.relative(buildDir, src)} -> ${path.relative(buildDir, dest)}`,
    );
  }
}
