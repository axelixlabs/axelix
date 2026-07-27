/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import { ArticleTable } from "@/app/styles/article-table/ArticleTable";
import { Mermaid } from "@/components/Mermaid";
import { withBlogBasePathForImageSrc } from "@/lib/url";

import { Callout } from "fumadocs-ui/components/callout";
import { CodeBlock, CodeBlockTabsList, CodeBlockTabsTrigger, Pre } from "fumadocs-ui/components/codeblock";
import { ImageZoom } from "fumadocs-ui/components/image-zoom";
import { Step, Steps } from "fumadocs-ui/components/steps";
import { Tab, Tabs } from "fumadocs-ui/components/tabs";
import defaultMdxComponents from "fumadocs-ui/mdx";
import * as icons from "lucide-react";
import type { MDXComponents } from "mdx/types";
import type { ReactNode } from "react";

// `remarkDirectiveAdmonition` (see source.config.ts) emits the triplet
// CalloutContainer / CalloutTitle / CalloutDescription for `:::tip[Title]`.
// Map the container onto fumadocs-ui's Callout via the same variant table the
// reference blog uses.
const calloutVariant: Record<string, "info" | "warn" | "error" | "success"> = {
    info: "info",
    note: "info",
    ppg: "info",
    tip: "success",
    success: "success",
    warning: "warn",
    warn: "warn",
    danger: "error",
    error: "error",
};

export function getMDXComponents(components?: MDXComponents): MDXComponents {
    const mdxComponents = {
        // All lucide icons available as MDX tags (parity with the reference).
        ...(icons as unknown as MDXComponents),
        // Base: fumadocs-ui renders every markdown element and is styled by its
        // CSS preset (imported in global.css).
        ...defaultMdxComponents,
        // Generic structural components for manual MDX usage.
        Tabs,
        Tab,
        Steps,
        Step,
        // ```mermaid blocks are rewritten to <Mermaid chart="…" /> by remarkMdxMermaid.
        Mermaid,
        // <ArticleTable name="…"> applies a scoped per-table CSS Module.
        ArticleTable,
        ...components,
        // Images/videos resolve through the blog basePath; images get zoom.
        img: (props: any) => <ImageZoom {...props} src={withBlogBasePathForImageSrc(props.src)} />,
        video: (props: any) => <video {...props} src={withBlogBasePathForImageSrc(props.src)} />,
    };

    return {
        ...mdxComponents,
        // Code blocks → fumadocs-ui CodeBlock card (border, language, copy button).
        pre: ({ ref: _ref, ...props }: any) => (
            <CodeBlock {...props}>
                <Pre>{props.children}</Pre>
            </CodeBlock>
        ),
        // Code-tab labels (e.g. Java | Kotlin) sit flush by default; add breathing
        // room between the triggers. `axx-code-tabs` scopes the active-tab styling
        // in global.css to code blocks only (not generic <Tabs>).
        CodeBlockTabsList: (props: any) => (
            <CodeBlockTabsList
                {...props}
                className={["gap-4 axx-code-tabs", props.className].filter(Boolean).join(" ")}
            />
        ),
        // Wrap the label in an inline span so the active underline (drawn in
        // global.css) can match the text width exactly.
        CodeBlockTabsTrigger: ({ children, ...props }: any) => (
            <CodeBlockTabsTrigger {...props}>
                <span className="axx-tab-label">{children}</span>
            </CodeBlockTabsTrigger>
        ),
        // Admonitions → fumadocs-ui Callout.
        CalloutTitle: ({ children }: { children?: ReactNode }) => <>{children}</>,
        CalloutDescription: ({ children }: { children?: ReactNode }) => <>{children}</>,
        CalloutContainer: ({ type, children, icon, ...props }: any) => (
            <Callout type={calloutVariant[type] ?? "info"} icon={icon} {...props}>
                {children}
            </Callout>
        ),
    };
}
