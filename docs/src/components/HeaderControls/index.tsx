"use client";

import { cn } from "cn";
import { Popover, PopoverContent, PopoverTrigger } from "fumadocs-ui/components/ui/popover";
import { useI18n } from "fumadocs-ui/contexts/i18n";
import { useNotebookLayout } from "fumadocs-ui/layouts/notebook";
import { Check, ChevronDown, Globe, Tag } from "lucide-react";
import { useState } from "react";

import { versions } from "@/lib/constants.mjs";

import styles from "./styles.module.css";

const CONTROL = "h-[34px] rounded-[9px]";

/**
 * Search, version, theme and language — the pill-shaped controls on the right of the header.
 * Each one is the built-in Fumadocs slot; only the chrome is ours.
 *
 * TODO: wrapper — customizes Fumadocs' search/theme/language slots (chrome only, behavior stock).
 */
export const HeaderControls = () => {
    const { slots } = useNotebookLayout();
    const { locale } = useI18n();
    const [versionOpen, setVersionOpen] = useState(false);
    const currentVersion = versions[0];

    return (
        <>
            {slots.searchTrigger && (
                <slots.searchTrigger.full
                    hideIfDisabled
                    className={cn(CONTROL, "w-[190px] shrink px-2.5 max-md:hidden")}
                />
            )}

            {/*
             * Decorative until there is a second documentation version to switch to: picking an
             * entry just closes the popover instead of navigating anywhere.
             *
             * TODO: when a real second version (e.g. v2) is added, revisit whether to switch to
             * Fumadocs' native version switcher (`root: "version"` in meta.json + `SidebarTabsDropdown`)
             * instead of this custom popover.
             */}
            <Popover open={versionOpen} onOpenChange={setVersionOpen}>
                <PopoverTrigger
                    className={cn(CONTROL, "flex items-center gap-1.5 border bg-fd-secondary/50 px-2.5 max-md:hidden")}
                >
                    <Tag className="size-3.5 text-fd-muted-foreground" />
                    <span className="font-mono text-[12.5px] font-medium">{currentVersion}</span>
                    <ChevronDown className="size-3 text-fd-muted-foreground" />
                </PopoverTrigger>
                <PopoverContent className="flex flex-col gap-1 p-1">
                    {versions.map((version) => (
                        <button
                            key={version}
                            type="button"
                            onClick={() => setVersionOpen(false)}
                            className="flex items-center gap-2 rounded-lg p-1.5 text-start text-sm hover:bg-fd-accent hover:text-fd-accent-foreground"
                        >
                            <span className="flex-1">{version}</span>
                            <Check
                                className={cn("size-3.5 text-fd-primary", version !== currentVersion && "invisible")}
                            />
                        </button>
                    ))}
                </PopoverContent>
            </Popover>

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
