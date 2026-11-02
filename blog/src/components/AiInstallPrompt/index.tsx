"use client";
import { useCopyButton } from "fumadocs-ui/utils/use-copy-button";
import { Check, Copy } from "lucide-react";
import type { ReactNode } from "react";
import { ClaudeCodeIcon, CodexIcon, CursorIcon } from "./icons";
import styles from "./styles.module.css";

const AI_INSTALL_PROMPT =
  "Install Axelix in my app: run `curl -s https://axelix.io/docs/start/SKILL.md` and use it as a guide";

interface Agent {
  name: string;
  icon: ReactNode;
  dark?: boolean;
}

const AGENTS: Agent[] = [
  { name: "Claude Code", icon: <ClaudeCodeIcon /> },
  { name: "Codex", icon: <CodexIcon />, dark: true },
  { name: "Cursor", icon: <CursorIcon /> },
];

export const AiInstallPrompt = () => {
  const [copied, onCopy] = useCopyButton(async () => {
    await navigator.clipboard.writeText(AI_INSTALL_PROMPT);
  });

  return (
    <div className={styles.Prompt} onClick={onCopy}>
      <ul className={styles.Agents} aria-hidden="true">
        {AGENTS.map(({ name, icon, dark }) => (
          <li key={name} className={dark ? `${styles.Agent} ${styles.Dark}` : styles.Agent} title={name}>
            {icon}
          </li>
        ))}
      </ul>
      <span className={styles.Caption} aria-hidden="true">
        Install Axelix with AI
      </span>
      <button
        className={copied ? `${styles.CopyButton} ${styles.Copied}` : styles.CopyButton}
        type="button"
        title={AI_INSTALL_PROMPT}
        aria-label="Copy the prompt to install Axelix with your AI agent"
      >
        <span className={styles.Labels}>
          <span className={styles.CopyLabel}>Copy prompt</span>
          <span className={styles.CopiedLabel}>Copied!</span>
        </span>
        <span className={styles.Icons}>
          <Copy className={styles.CopyLabel} />
          <Check className={styles.CopiedLabel} />
        </span>
      </button>
      <span className="sr-only" aria-live="polite">
        {copied ? "Prompt copied" : ""}
      </span>
    </div>
  );
};
