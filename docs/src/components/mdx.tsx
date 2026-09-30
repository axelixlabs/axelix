import { cn } from "cn";
import { Tab, Tabs } from "fumadocs-ui/components/tabs";
import defaultMdxComponents from "fumadocs-ui/mdx";
import type { MDXComponents } from "mdx/types";
import NextImage, { type ImageProps } from "next/image";

import { LegacyNotice, ReleasedInNotice, UpcomingReleaseNotice } from "@/components/ReleaseNotices";
import { BASE_PATH } from "@/lib/constants.mjs";

/**
 * TODO: wrapper — gives root-relative public images the base path required by NextImage.
 */
function Image({ src, className, ...props }: ImageProps) {
    return (
        <NextImage
            src={typeof src === "string" && src.startsWith("/") ? `${BASE_PATH}${src}` : src}
            className={cn("inline not-prose", className)}
            {...props}
        />
    );
}

/**
 * Components every article can use without importing them. `Tabs`/`Tab` and the release notices
 * are used across most topics, so they are provided here rather than imported page by page.
 *
 * TODO: wrapper — exposes our custom ReleaseNotices components as ambient MDX globals.
 */
export function getMDXComponents(components?: MDXComponents) {
    return {
        ...defaultMdxComponents,
        Tabs,
        Tab,
        Image,
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
