import type { MDXComponents } from "mdx/types";
import type { ReactNode } from "react";
import defaultMdxComponents from "fumadocs-ui/mdx";
import { Callout } from "fumadocs-ui/components/callout";
import {
  CodeBlock,
  CodeBlockTabsList,
  CodeBlockTabsTrigger,
  Pre,
} from "fumadocs-ui/components/codeblock";
import { Tab, Tabs } from "fumadocs-ui/components/tabs";
import { Step, Steps } from "fumadocs-ui/components/steps";
import { ImageZoom } from "fumadocs-ui/components/image-zoom";
import * as icons from "lucide-react";
import { Mermaid } from "@/components/Mermaid";
import { ArticleTable } from "@/app/styles/article-table/ArticleTable";
import { withBlogBasePathForImageSrc } from "@/lib/url";

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
    img: (props: any) => (
      <ImageZoom {...props} src={withBlogBasePathForImageSrc(props.src)} />
    ),
    video: (props: any) => (
      <video {...props} src={withBlogBasePathForImageSrc(props.src)} />
    ),
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
