"use client";

import { cn } from "cn";
import { useI18n } from "fumadocs-ui/contexts/i18n";
import { LanguageSelect } from "fumadocs-ui/layouts/shared/slots/language-select";
import type { LanguageSelectProps } from "fumadocs-ui/layouts/shared/slots/language-select";
import { ThemeSwitch } from "fumadocs-ui/layouts/shared/slots/theme-switch";
import type { ThemeSwitchProps } from "fumadocs-ui/layouts/shared/slots/theme-switch";
import { ChevronDown, Globe } from "lucide-react";

import styles from "@/components/HeaderControls/styles.module.css";

const PILL = "h-[34px] gap-1.5 rounded-[9px] bg-fd-secondary/50 px-2.5";

/**
 * `HeaderControls` styles the desktop language/theme controls itself (explicit `className` and
 * `children` on every call, which always win over what's set here). Fumadocs also mounts these
 * same slots on its own, with no styling, in the mobile sidebar drawer footer — this is the
 * default those call sites fall back to, so the drawer matches the desktop pill design.
 *
 * TODO: wrapper — default `languageSelect`/`themeSwitch` slots for `DocsLayout` (`[lang]/layout.tsx`).
 */
export const LanguageSelectSlot = ({ className, ...props }: LanguageSelectProps) => {
    const { locale } = useI18n();

    return (
        // fumadocs' own drawer footer sets no gap between its children, so the default (no
        // `className` passed) needs its own spacing; `HeaderControls` always passes one, so
        // this never doubles up with its `gap-3` row there.
        <LanguageSelect variant="outline" className={cn(PILL, !className && "me-2", className)} {...props}>
            <Globe className="size-3.5 text-fd-muted-foreground" />
            <span className="font-mono text-[12.5px] font-medium">{locale?.toUpperCase()}</span>
            <ChevronDown className="size-3 text-fd-muted-foreground" />
        </LanguageSelect>
    );
};

export const ThemeSwitchSlot = ({ className, ...props }: ThemeSwitchProps) => (
    <ThemeSwitch mode="light-dark" className={cn(styles.ThemeSwitch, className)} {...props} />
);
