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
import "./global.css";

import { BLOG_HOME_DESCRIPTION, BLOG_HOME_TITLE, SITE_NAME } from "@/lib/blog-metadata";
import { getBaseUrl, withBlogBasePath } from "@/lib/url";

import { RootProvider } from "fumadocs-ui/provider/next";
import type { Metadata } from "next";
import type { CSSProperties, ReactNode } from "react";

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
                <RootProvider theme={{ enabled: false }} search={{ options: { api: withBlogBasePath("/api/search") } }}>
                    {children}
                </RootProvider>
            </body>
        </html>
    );
}
