"use client";

import { useTranslations } from "@fuma-translate/react";

import { cn } from "cn";
import { useCopyButton } from "fumadocs-ui/utils/use-copy-button";
import { Check, Copy } from "lucide-react";
import type { ReactNode } from "react";

import { AI_INSTALL_PROMPT } from "@/lib/shared";

import { ClaudeCodeIcon, CodexIcon, CursorIcon } from "./icons";
import styles from "./styles.module.css";

type Agent = {
    name: string;
    icon: ReactNode;
    dark?: boolean;
};

const AGENTS: Agent[] = [
    { name: "Claude Code", icon: <ClaudeCodeIcon /> },
    { name: "Codex", icon: <CodexIcon />, dark: true },
    { name: "Cursor", icon: <CursorIcon /> },
];

/** TODO: custom — based on website/src/components/AiInstallPrompt, no Fumadocs equivalent. */
export const AiInstallPrompt = () => {
    const t = useTranslations({ note: "ai prompt" });
    const [copied, onCopy] = useCopyButton(() => navigator.clipboard.writeText(AI_INSTALL_PROMPT));

    return (
        <div className={styles.Prompt} onClick={onCopy}>
            <ul className={styles.Agents} aria-hidden="true">
                {AGENTS.map(({ name, icon, dark }) => (
                    <li key={name} className={cn(styles.Agent, dark && styles.Dark)} title={name}>
                        {icon}
                    </li>
                ))}
            </ul>
            <span className={styles.Caption} aria-hidden="true">
                {t("Install Axelix with AI")}
            </span>
            <button
                className={cn(styles.CopyButton, copied && styles.Copied)}
                type="button"
                title={AI_INSTALL_PROMPT}
                aria-label={t("Copy prompt for your AI agent", { note: "aria-label" })}
            >
                <span className={styles.Labels}>
                    <span className={styles.CopyLabel}>{t("Copy prompt")}</span>
                    <span className={styles.CopiedLabel}>{t("Copied!")}</span>
                </span>
                <span className={styles.Icons}>
                    <Copy className={styles.CopyLabel} />
                    <Check className={styles.CopiedLabel} />
                </span>
            </button>
            <span className="sr-only" aria-live="polite">
                {copied ? t("Prompt copied") : ""}
            </span>
        </div>
    );
};
