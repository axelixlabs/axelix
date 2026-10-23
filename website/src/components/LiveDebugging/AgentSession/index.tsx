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
import { useEffect, useState } from "react";

import { PROMPT, STEPS } from "../steps";

import styles from "./styles.module.css";

const SPINNER_FRAMES = ["⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"];
const TICK_MS = 120;

interface ISessionState {
    chars: number;
    shown: number;
    done: number;
    thinking: boolean;
    summary: boolean;
}

const INITIAL_STATE: ISessionState = { chars: 0, shown: 0, done: 0, thinking: false, summary: false };
const FINAL_STATE: ISessionState = {
    chars: PROMPT.length,
    shown: STEPS.length,
    done: STEPS.length,
    thinking: false,
    summary: true,
};

// Replays an AI agent debugging an incident through the Axelix MCP tools, in a loop.
export const AgentSession = () => {
    const [session, setSession] = useState<ISessionState>(INITIAL_STATE);
    const [tick, setTick] = useState(0);

    useEffect(() => {
        const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
        let alive = true;
        let timer: number | undefined;
        const wait = (ms: number) => new Promise<void>((resolve) => (timer = window.setTimeout(resolve, ms)));
        const update = (patch: Partial<ISessionState>) => setSession((prev) => ({ ...prev, ...patch }));
        const ticker = reducedMotion ? undefined : window.setInterval(() => setTick((prev) => prev + 1), TICK_MS);

        const run = async () => {
            if (reducedMotion) {
                setSession(FINAL_STATE);
                return;
            }
            while (alive) {
                setSession(INITIAL_STATE);
                await wait(900);
                for (let i = 1; i <= PROMPT.length && alive; i++) {
                    update({ chars: i });
                    await wait(28 + Math.random() * 40);
                }
                await wait(500);
                for (let i = 1; i <= STEPS.length && alive; i++) {
                    update({ thinking: true });
                    await wait(900);
                    update({ thinking: false, shown: i });
                    await wait(1100);
                    update({ done: i });
                    await wait(700);
                }
                update({ thinking: true });
                await wait(1200);
                update({ thinking: false, summary: true });
                await wait(7000);
            }
        };

        run();

        return () => {
            alive = false;
            window.clearTimeout(timer);
            window.clearInterval(ticker);
        };
    }, []);

    const { chars, shown, done, thinking, summary } = session;
    const spinner = SPINNER_FRAMES[tick % SPINNER_FRAMES.length];
    const typing = chars < PROMPT.length;
    const caretVisible = !shown && (typing || Math.floor(tick / 4) % 2 === 0);

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.Header}>
                <span>claude code · payments-service</span>
                <span className={styles.Status}>axelix mcp connected</span>
            </div>
            <div className={styles.Body}>
                <div className={styles.Prompt}>
                    <span className={styles.PromptMark}>&gt;</span>
                    <span>
                        {PROMPT.slice(0, chars)}
                        <span className={styles.Caret} style={{ opacity: caretVisible ? 1 : 0 }} />
                    </span>
                </div>
                {STEPS.slice(0, shown).map(({ note, tool, args, result, color }, index) => {
                    const finished = index < done;

                    return (
                        <div key={index} className={styles.Step}>
                            {note && <div className={styles.Note}>{note}</div>}
                            <div className={styles.Call}>
                                <span className={styles.CallMark}>●</span>
                                <span className={styles.Tool}>{tool}</span>
                                <span className={styles.Args}>{args}</span>
                            </div>
                            <div className={styles.Result} style={{ color: finished ? color : undefined }}>
                                <span className={styles.ResultMark}>└</span>
                                <span>{finished ? result : `${spinner} running…`}</span>
                            </div>
                        </div>
                    );
                })}
                {thinking && <div className={styles.Thinking}>{spinner} thinking…</div>}
                {summary && (
                    <div className={styles.Summary}>
                        <b>Root cause:</b> <code>payments.auth.mode</code> is set to{" "}
                        <code className={styles.Wrong}>oath</code>, not <code className={styles.Right}>oauth</code>. The
                        condition on <code>OAuthPaymentClientConfig</code> never matched, so the no-op client sends
                        requests without a token. Fix the value in application-prod.yml.
                    </div>
                )}
            </div>
        </div>
    );
};
