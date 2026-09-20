// Shared by `next.config.mjs` and the TypeScript sources, so the values below
// are declared exactly once.

/** Sub-path the app is deployed under; also Next.js `basePath`. */
export const BASE_PATH = "/docs";

/** Locale served without a URL prefix. */
/** @type {'en'} */
export const DEFAULT_LOCALE = "en";

/** Available documentation versions. */
export const VERSION_1_X = "1.x";

/** Documentation version used by unversioned URLs and links to the latest docs. */
export const LATEST_VERSION = VERSION_1_X;

/** All documentation versions shown in the version selector. */
export const versions = [VERSION_1_X];

/**
 * Locales served under a URL prefix. The default locale is served without one.
 *
 * @type {['ru']}
 */
export const prefixedLocales = ["ru"];
