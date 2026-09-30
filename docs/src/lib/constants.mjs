// Shared by `next.config.mjs` and the TypeScript sources, so the values below
// are declared exactly once.

/** Sub-path the app is deployed under; also Next.js `basePath`. */
export const BASE_PATH = "/docs";

/** Locale served without a URL prefix. */
/** @type {'en'} */
export const DEFAULT_LOCALE = "en";

/**
 * Documentation versions shown in the (currently decorative) version switcher. Purely a
 * display list — content is unversioned in the URL until a second entry actually exists.
 */
export const versions = ["1.x"];

/**
 * Locales served under a URL prefix. The default locale is served without one.
 *
 * @type {['ru']}
 */
export const prefixedLocales = ["ru"];
