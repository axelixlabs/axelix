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
"use client";
import {
    ClaudeCodeIcon,
    CodexIcon,
    CopilotIcon,
    CursorIcon,
    GeminiCliIcon,
    OpenCodeIcon,
    PiIcon,
    WindsurfIcon,
} from "@/assets";
import { useCopyToClipboard } from "@/hooks/useCopyToClipboard";
import { IAgent } from "@/models";
import { AI_INSTALL_PROMPT } from "@/utils";

import { useEffect, useRef, useState } from "react";

import styles from "./styles.module.css";

// Dark tiles alternate with light ones, so two of them never merge in the overlapping row.
// The Codex and Gemini CLI gradient ids repeat in every instance: never display: none one while another is shown.
const AGENTS: IAgent[] = [
    { name: "Claude Code", icon: <ClaudeCodeIcon /> },
    { name: "GitHub Copilot", icon: <CopilotIcon />, dark: true },
    { name: "Codex", icon: <CodexIcon /> },
    { name: "Windsurf", icon: <WindsurfIcon />, dark: true },
    { name: "Cursor", icon: <CursorIcon /> },
    { name: "OpenCode", icon: <OpenCodeIcon />, dark: true },
    { name: "Gemini CLI", icon: <GeminiCliIcon /> },
    { name: "Pi", icon: <PiIcon />, dark: true },
];

const VISIBLE_AGENTS = 5;
const ROTATE_INTERVAL_MS = 2000;

const nextTick = (current: number): number => current + 1;

interface IProps {
    /**
     * Tighter padding and a smaller button, for narrow columns
     */
    compact?: boolean;
}

export const AiInstallPrompt = ({ compact = false }: IProps) => {
    const [tick, setTick] = useState<number>(0);
    const [copied, copy] = useCopyToClipboard();
    const hoveredRef = useRef<boolean>(false);
    const focusedRef = useRef<boolean>(false);

    useEffect(() => {
        if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
            return;
        }

        const interval = setInterval(() => {
            if (!hoveredRef.current && !focusedRef.current) {
                setTick(nextTick);
            }
        }, ROTATE_INTERVAL_MS);

        return () => clearInterval(interval);
    }, []);

    const handleCopy = async (): Promise<void> => {
        await copy(AI_INSTALL_PROMPT);
    };

    const handleMouseEnter = (): void => {
        hoveredRef.current = true;
    };

    const handleMouseLeave = (): void => {
        hoveredRef.current = false;
    };

    const handleFocus = (): void => {
        focusedRef.current = true;
    };

    const handleBlur = (): void => {
        focusedRef.current = false;
    };

    // Keys are queue positions, so the agents that stay keep their DOM nodes and slide.
    const positions = Array.from({ length: VISIBLE_AGENTS + 1 }, (_, index) => tick - 1 + index).filter(
        (position) => position >= 0,
    );

    return (
        <div
            className={`ShimmerBorder ${styles.MainWrapper} ${compact ? styles.Compact : ""}`}
            onMouseEnter={handleMouseEnter}
            onMouseLeave={handleMouseLeave}
            onFocus={handleFocus}
            onBlur={handleBlur}
        >
            <ul className={styles.Agents} aria-hidden="true">
                {positions.map((position, index) => {
                    const { name, icon, dark } = AGENTS[position % AGENTS.length];
                    const leaving = tick > 0 && index === 0;
                    const entering = tick > 0 && index === positions.length - 1;

                    return (
                        <li
                            key={position}
                            className={`${styles.Agent} ${dark ? styles.Dark : ""} ${leaving ? styles.Leaving : ""} ${entering ? styles.Entering : ""}`}
                            style={{ zIndex: positions.length - index }}
                            title={name}
                        >
                            {icon}
                        </li>
                    );
                })}
            </ul>
            <span className={styles.Caption}>Install with your AI agent</span>
            <button
                className={`${styles.CopyButton} ${copied ? styles.Copied : ""}`}
                type="button"
                title={AI_INSTALL_PROMPT}
                aria-label="Copy prompt for your AI agent"
                onClick={handleCopy}
            >
                <span className={styles.CopyLabel}>Copy prompt</span>
                <span className={styles.CopiedLabel}>Copied!</span>
            </button>
            <span className={styles.Status} aria-live="polite">
                {copied ? "Prompt copied" : ""}
            </span>
        </div>
    );
};
