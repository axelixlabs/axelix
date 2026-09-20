import { describe, expect, it } from "vitest";

import { DEFAULT_LOCALE, LATEST_VERSION } from "../lib/constants.mjs";
import { splitLocale } from "../proxy";

describe("splitLocale", () => {
    it.each([
        ["/", DEFAULT_LOCALE, "/"],
        [`/${LATEST_VERSION}`, DEFAULT_LOCALE, `/${LATEST_VERSION}`],
        [`/${LATEST_VERSION}/test`, DEFAULT_LOCALE, `/${LATEST_VERSION}/test`],
        ["/ru", "ru", "/"],
        [`/ru/${LATEST_VERSION}`, "ru", `/${LATEST_VERSION}`],
        [`/ru/${LATEST_VERSION}/test`, "ru", `/${LATEST_VERSION}/test`],
        // `en` is served without a prefix, but the proxy still has to recognise it
        [`/en/${LATEST_VERSION}`, "en", `/${LATEST_VERSION}`],
        // not a locale, so the whole path belongs to the default locale
        ["/rust/guide", DEFAULT_LOCALE, "/rust/guide"],
        ["//x", DEFAULT_LOCALE, "//x"],
        ["/llms.txt", DEFAULT_LOCALE, "/llms.txt"],
        ["/ru/llms.txt", "ru", "/llms.txt"],
    ])("splits %s into %s + %s", (pathname, locale, path) => {
        expect(splitLocale(pathname)).toEqual({ locale, path });
    });
});
