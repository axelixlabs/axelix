import { cn } from "cn";
import { ImageZoom } from "fumadocs-ui/components/image-zoom";
import { Tab, Tabs } from "fumadocs-ui/components/tabs";
import defaultMdxComponents from "fumadocs-ui/mdx";
import type { MDXComponents } from "mdx/types";
import NextImage, { type ImageProps } from "next/image";
import type { ImgHTMLAttributes } from "react";

import { LegacyNotice, ReleasedInNotice, ScreenshotsNotice, UpcomingReleaseNotice } from "@/components/ReleaseNotices";
import { BASE_PATH } from "@/lib/constants.mjs";

/**
 * TODO: wrapper — gives root-relative public images the base path required by NextImage.
 */
function Image({ src, className, ...props }: ImageProps) {
    const imageSrc = typeof src === "string" && src.startsWith("/") ? `${BASE_PATH}${src}` : src;

    return (
        <ImageZoom src={imageSrc} zoomInProps={typeof imageSrc === "string" ? { src: imageSrc } : undefined}>
            <NextImage src={imageSrc} className={cn("inline not-prose", className)} {...props} />
        </ImageZoom>
    );
}

type MarkdownImageProps = Omit<ImgHTMLAttributes<HTMLImageElement>, "src"> & {
    src?: ImageProps["src"] | Blob;
};

function MarkdownImage({ src, alt = "", width, height, ...props }: MarkdownImageProps) {
    return (
        <Image
            {...props}
            src={src instanceof Blob ? "" : (src ?? "")}
            alt={alt}
            width={width as ImageProps["width"]}
            height={height as ImageProps["height"]}
            className="rounded-lg"
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
        img: MarkdownImage,
        Image,
        ReleasedInNotice,
        UpcomingReleaseNotice,
        LegacyNotice,
        ScreenshotsNotice,
        ...components,
    } satisfies MDXComponents;
}

export const useMDXComponents = getMDXComponents;

declare global {
    type MDXProvidedComponents = ReturnType<typeof getMDXComponents>;
}
