import { i18nProvider } from "fumadocs-ui/i18n";

import "../global.css";

import { DocsLayout } from "fumadocs-ui/layouts/notebook";
import { getLayoutTabs } from "fumadocs-ui/layouts/shared";
import { RootProvider } from "fumadocs-ui/provider/next";

import { DocsHeader } from "@/components/DocsHeader";
import { baseOptions, translations } from "@/lib/layout.shared";
import { BASE_PATH } from "@/lib/shared";
import { source } from "@/lib/source";

export default async function Layout({ params, children }: LayoutProps<"/[lang]">) {
    const { lang } = await params;
    const { nav, ...base } = baseOptions(lang);
    const tree = source.getPageTree(lang);

    return (
        <html lang={lang} suppressHydrationWarning>
            <body className="flex flex-col min-h-screen">
                <RootProvider
                    i18n={i18nProvider(translations, lang)}
                    // the search client resolves its default API path from a Vite env var,
                    // which Next never sets, so `BASE_PATH` has to be applied here
                    search={{ options: { api: `${BASE_PATH}/api/search` } }}
                >
                    <DocsLayout
                        tree={tree}
                        tabMode="navbar"
                        // topics are plain sidebar groups, not tabs — this only supplies the
                        // version, which `HeaderControls` reads out of the same array
                        tabs={getLayoutTabs(tree)}
                        // the design has no collapse control, so the sidebar must not
                        // offer a collapsed state at all
                        sidebar={{ collapsible: false }}
                        slots={{ header: DocsHeader }}
                        {...base}
                        // `top` lifts the header above the sidebar, the way the design has it
                        nav={{ ...nav, mode: "top" }}
                    >
                        {children}
                    </DocsLayout>
                </RootProvider>
            </body>
        </html>
    );
}
