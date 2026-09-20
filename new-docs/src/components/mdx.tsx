import { Tab, Tabs } from "fumadocs-ui/components/tabs";
import defaultMdxComponents from "fumadocs-ui/mdx";
import type { MDXComponents } from "mdx/types";

import { LegacyNotice, ReleasedInNotice, UpcomingReleaseNotice } from "@/components/ReleaseNotices";

/**
 * Components every article can use without importing them. `Tabs`/`Tab` and the release notices
 * are used across most topics, so they are provided here rather than imported page by page.
 */
export function getMDXComponents(components?: MDXComponents) {
    return {
        ...defaultMdxComponents,
        Tabs,
        Tab,
        ReleasedInNotice,
        UpcomingReleaseNotice,
        LegacyNotice,
        ...components,
    } satisfies MDXComponents;
}

export const useMDXComponents = getMDXComponents;

declare global {
    type MDXProvidedComponents = ReturnType<typeof getMDXComponents>;
}
