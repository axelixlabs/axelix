import { Footer, Header, Metric } from "@/components";

import type { Metadata } from "next";
import { Golos_Text, JetBrains_Mono } from "next/font/google";

import "./globals.css";

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
                <Metric />
                <Header />
                <main>{children}</main>
                <Footer />
            </body>
        </html>
    );
}
