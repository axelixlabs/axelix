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
import { Footer, Header, Metric } from "@/components";
import { AxelixVersionProvider } from "@/hooks/useAxelixVersion";
import { SITE_URL } from "@/utils";

import type { Metadata } from "next";
import { Golos_Text, JetBrains_Mono } from "next/font/google";

import "./globals.css";

/**
 * Site-wide metadata for axelix.io, injected by Next.js into <head> on every route.
 * Lines marked `EDIT:` carry example copy — refine the wording; the rest is structural.
 * Future sub-pages can export their own `metadata` to override any field (their title
 * flows through the `title.template` below automatically).
 */
export const metadata: Metadata = {
    // Absolute origin used to turn every relative URL below (canonical, OG image) into
    // an absolute one. Must be the production origin. Defined once in src/utils.
    metadataBase: new URL(SITE_URL),

    title: {
        // Homepage <title>: the browser-tab text AND the clickable blue line in Google
        // results. Contains the core value of the brand. Touch wisely.
        default: "Axelix — AI-Native Quality Control & Observability tooling for Spring Boot",
        // Wraps every sub-page's title, e.g. a future /pricing → "Pricing | Axelix".
        template: "%s | Axelix",
    },

    // The grey snippet under the title in search results, and the fallback text for
    // social shares. Touch wisely.
    // EDIT:
    description:
        "Axelix streamlines the development of the Java Spring Boot ecosystems by ensuring the quality and observability across the entire Java fleet",

    // Product name; some browsers/PWAs surface it. Keep it the brand.
    applicationName: "Axelix",
    // Who publishes the site — harmless, mildly useful to crawlers.:
    publisher: "Axelix Labs",

    // Canonical URL of this page: tells Google the homepage is "/" (resolved against
    // metadataBase → https://axelix.io/), consolidating any ?utm=/#hash variants.
    alternates: {
        canonical: "/",
    },

    // Open Graph — how the link looks when shared on LinkedIn, Slack, Facebook, Discord,
    // WhatsApp, etc. `title`/`description` are inherited from the top-level fields above,
    // so only override them here for different social copy:
    openGraph: {
        type: "website",
        url: "/", // → https://axelix.io/
        siteName: "Axelix",
        locale: "en_US",
        // Share preview image. Has 1200×630 px resolution as the general best practise.
        // "/og-image.png" resolves to https://axelix.io/og-image.png:
        images: [
            {
                url: "/og-image.png",
                width: 1200,
                height: 630,
                alt: "Axelix Logo",
            },
        ],
    },

    // Twitter/X card. `summary_large_image` renders the big banner (needs the image, or it
    // degrades to a tiny card). title/description are also inherited from the top level.
    twitter: {
        card: "summary_large_image",
    },

    // Crawling policy. index + follow is already the default, so this block is optional —
    // kept explicit to show intent.
    robots: { index: true, follow: true },
};

const golosText = Golos_Text({
    variable: "--font-golos-text",
    subsets: ["latin"],
    weight: ["400", "500", "600", "700"],
});

const jetBrainsMono = JetBrains_Mono({
    variable: "--font-jetbrains-mono",
    subsets: ["latin"],
    weight: ["400", "500"],
});

export default function RootLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="en" className={`${golosText.variable} ${jetBrainsMono.variable}`}>
            <body>
                <AxelixVersionProvider>
                    <Metric />
                    <Header />
                    <main>{children}</main>
                    <Footer />
                </AxelixVersionProvider>
            </body>
        </html>
    );
}
