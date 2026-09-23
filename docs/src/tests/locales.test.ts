import { describe, expect, it } from "vitest";

import { DEFAULT_LOCALE } from "../lib/constants.mjs";
import { splitLocale } from "../proxy";

describe("splitLocale", () => {
    it.each([
        ["/", DEFAULT_LOCALE, "/"],
        ["/test", DEFAULT_LOCALE, "/test"],
        ["/product/guide", DEFAULT_LOCALE, "/product/guide"],
        ["/ru", "ru", "/"],
        ["/ru/test", "ru", "/test"],
        ["/ru/product/guide", "ru", "/product/guide"],
        // `en` is served without a prefix, but the proxy still has to recognise it
        ["/en/test", "en", "/test"],
        // not a locale, so the whole path belongs to the default locale
        ["/rust/guide", DEFAULT_LOCALE, "/rust/guide"],
        ["//x", DEFAULT_LOCALE, "//x"],
        ["/llms.txt", DEFAULT_LOCALE, "/llms.txt"],
        ["/ru/llms.txt", "ru", "/llms.txt"],
    ])("splits %s into %s + %s", (pathname, locale, path) => {
        expect(splitLocale(pathname)).toEqual({ locale, path });
    });
});
