"use client";

import { cn } from "cn";
import { SidebarTabsDropdown } from "fumadocs-ui/components/sidebar/tabs/dropdown";
import { useI18n } from "fumadocs-ui/contexts/i18n";
import { useNotebookLayout } from "fumadocs-ui/layouts/notebook";
import { ChevronDown, Globe } from "lucide-react";

import styles from "./styles.module.css";

const CONTROL = "h-[34px] rounded-[9px]";

/**
 * Search, version, theme and language — the pill-shaped controls on the right of the header.
 * Each one is the built-in Fumadocs slot; only the chrome is ours.
 */
export const HeaderControls = () => {
    const {
        slots,
        props: { tabs },
    } = useNotebookLayout();
    const { locale } = useI18n();

    const versionTabs = tabs.filter((tab) => typeof tab.$folder?.root === "string");

    return (
        <>
            {slots.searchTrigger && (
                <slots.searchTrigger.full
                    hideIfDisabled
                    className={cn(CONTROL, "w-[190px] shrink px-2.5 max-md:hidden")}
                />
            )}

            {versionTabs.length > 0 && (
                <SidebarTabsDropdown options={versionTabs} className={cn(CONTROL, "px-2.5 max-md:hidden")} />
            )}

            {slots.themeSwitch && (
                <slots.themeSwitch mode="light-dark" className={cn(styles.ThemeSwitch, "max-md:hidden")} />
            )}

            {slots.languageSelect && (
                <slots.languageSelect.root
                    variant="outline"
                    className={cn(CONTROL, "gap-1.5 bg-fd-secondary/50 px-2.5 max-md:hidden")}
                >
                    <Globe className="size-3.5 text-fd-muted-foreground" />
                    <span className="font-mono text-[12.5px] font-medium">{locale?.toUpperCase()}</span>
                    <ChevronDown className="size-3 text-fd-muted-foreground" />
                </slots.languageSelect.root>
            )}
        </>
    );
};
