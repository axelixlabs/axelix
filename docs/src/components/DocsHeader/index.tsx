"use client";

import { useTranslations } from "@fuma-translate/react";

import { cn } from "cn";
import Link from "fumadocs-core/link";
import { useI18n } from "fumadocs-ui/contexts/i18n";
import { useNotebookLayout } from "fumadocs-ui/layouts/notebook";
import { LinkItem } from "fumadocs-ui/layouts/shared";
import type { MainItemType } from "fumadocs-ui/layouts/shared";
import { Sidebar } from "lucide-react";
import type { ComponentProps } from "react";

import { HeaderControls } from "@/components/HeaderControls";
import { getDocsUrl } from "@/lib/shared";

const ICON_BUTTON =
    "inline-flex size-[30px] items-center justify-center rounded-lg text-fd-muted-foreground transition-colors hover:bg-fd-muted hover:text-fd-foreground [&_svg]:size-[19px]";

/**
 * Documentation header laid out as a single row, per the design mock.
 *
 * Replaces the default Notebook header through the `slots.header` API: the arrangement
 * is ours, every interactive part is the built-in Fumadocs component.
 *
 * TODO: wrapper — overrides Fumadocs' default header via the slots.header API.
 */
export const DocsHeader = ({ className, ...props }: ComponentProps<"header">) => {
    const { slots, navItems } = useNotebookLayout();
    const { locale } = useI18n();
    const t = useTranslations();

    return (
        <header
            id="nd-subnav"
            className={cn(
                // `grid-area: header` is where the Notebook layout expects its header. Spanning
                // the grid by line numbers instead made the header's min-content the floor for
                // both gutter tracks, which are the only ones sized intrinsically — that pinned
                // them at ~267px each and starved the content column.
                "sticky top-(--fd-docs-row-1) z-10 mb-[7px] flex h-[58px] items-center gap-3 border-b px-5 backdrop-blur-[12px] [grid-area:header] layout:[--fd-header-height:58px]",
                "bg-[color-mix(in_srgb,var(--color-fd-background)_88%,transparent)]",
                className,
            )}
            {...props}
        >
            <div className="flex flex-none items-center gap-2.5">
                {slots.navTitle && <slots.navTitle className="flex items-center gap-2.5" />}
                <span className="font-mono text-base text-fd-muted-foreground max-sm:hidden">/</span>
                <Link
                    href={getDocsUrl([], locale)}
                    className="font-mono text-[15px] font-bold text-fd-muted-foreground transition-colors hover:text-fd-foreground max-sm:hidden"
                >
                    {t("Docs", { note: "header" })}
                </Link>
            </div>

            <nav className="flex min-w-0 flex-1 items-center gap-0.5 overflow-x-auto max-md:hidden">
                {navItems
                    .filter((item): item is MainItemType => item.type === "main" || item.type === undefined)
                    .map((item) => (
                        <LinkItem
                            key={item.url}
                            item={item}
                            className="shrink-0 rounded-[7px] px-2.5 py-1.5 text-sm font-medium whitespace-nowrap text-fd-muted-foreground transition-colors hover:bg-fd-muted hover:text-fd-foreground data-[active=true]:bg-fd-primary/10 data-[active=true]:text-fd-primary"
                        >
                            {item.text}
                        </LinkItem>
                    ))}
            </nav>

            <div className="flex flex-none items-center justify-end gap-3 max-md:ml-auto">
                <HeaderControls />

                <div className="flex items-center gap-0.5 max-lg:hidden">
                    {navItems
                        .filter((item) => item.type === "icon")
                        .map((item) => (
                            <LinkItem key={item.url} item={item} aria-label={item.label} className={ICON_BUTTON}>
                                {item.icon}
                            </LinkItem>
                        ))}
                </div>

                <div className="flex items-center gap-1 md:hidden">
                    {slots.searchTrigger && <slots.searchTrigger.sm hideIfDisabled className="p-2" />}
                    {slots.sidebar && (
                        <slots.sidebar.trigger className={cn(ICON_BUTTON, "size-8")}>
                            <Sidebar />
                        </slots.sidebar.trigger>
                    )}
                </div>
            </div>
        </header>
    );
};
