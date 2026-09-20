import { defineI18n } from "fumadocs-core/i18n";

import { DEFAULT_LOCALE, prefixedLocales } from "./constants.mjs";

export const i18n = defineI18n({
    languages: [DEFAULT_LOCALE, ...prefixedLocales],
    defaultLanguage: DEFAULT_LOCALE,
    hideLocale: "default-locale",
});
