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
import { Footer, Header } from "@/components";

import type { Metadata } from "next";
import { Golos_Text, JetBrains_Mono } from "next/font/google";

import "./globals.css";

import styles from "./styles.module.css";

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

export const metadata: Metadata = {
    title: "Axelix — AI monitoring for Spring Boot in production",
    description:
        "Axelix is the open-source console for debugging, observing and operating mission-critical Spring Boot microservices.",
};

export default function RootLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="en" className={`${golosText.variable} ${jetBrainsMono.variable}`}>
            <body>
                <Header />
                <main className={styles.ChildrenWrapper}>{children}</main>
                <Footer />
            </body>
        </html>
    );
}
