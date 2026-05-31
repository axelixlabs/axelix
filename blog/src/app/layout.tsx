import "./global.css";
import type { Metadata } from "next";
import type { CSSProperties, ReactNode } from "react";
import { RootProvider } from "fumadocs-ui/provider/next";
import { BLOG_HOME_TITLE, BLOG_HOME_DESCRIPTION, SITE_NAME } from "@/lib/blog-metadata";
import { getBaseUrl, withBlogBasePath } from "@/lib/url";

export const metadata: Metadata = {
  // Origin only (no /blog): page metadata supplies the basePath via
  // `withBlogBasePath` in the canonical/og:url, so it must not be doubled here.
  metadataBase: new URL(getBaseUrl()),
  title: {
    default: BLOG_HOME_TITLE,
    template: `%s — ${SITE_NAME} Blog`,
  },
  description: BLOG_HOME_DESCRIPTION,
};

// Map the design's font CSS variables to the self-hosted families declared via
// @font-face in src/app/styles/fonts.css (no Google Fonts CDN).
const fontVars: CSSProperties = {
  ["--font-golos" as string]: "'Golos Text'",
  ["--font-jetbrains" as string]: "'JetBrains Mono'",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en" style={fontVars} data-scroll-behavior="smooth">
      {/* Browser extensions (ColorZilla, Grammarly, …) inject attributes like
          `cz-shortcut-listen` on <body> before React hydrates, which trips the
          hydration-mismatch warning. Suppress it for <body>'s own attributes. */}
      <body suppressHydrationWarning>
        {/* RootProvider supplies the search context + ⌘K dialog. The search API
            lives under the /blog basePath, which client fetch() does not prefix
            automatically — so point it at the explicit path. next-themes is
            disabled: the blog is a single light theme with its own tokens. */}
        <RootProvider
          theme={{ enabled: false }}
          search={{ options: { api: withBlogBasePath("/api/search") } }}
        >
          {children}
        </RootProvider>
      </body>
    </html>
  );
}
