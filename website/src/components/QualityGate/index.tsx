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
import { useEffect, useRef, useState } from "react";

import { SectionLabel } from "../SectionLabel";

import { CodePage } from "./CodePage";
import { FindingsPage } from "./FindingsPage";
import { QUALITY_EXAMPLES } from "./items";
import styles from "./styles.module.css";

const FLIP_DURATION_MS = 1200;

/*
 * A two-page spread: findings on the left, code on the right. Turning the page rotates the code page to the left,
 * revealing the next example's code underneath while its back face becomes the next findings page.
 */
export const QualityGate = () => {
    const [index, setIndex] = useState(0);
    // The example the page is turning to, or null while the spread is at rest.
    const [target, setTarget] = useState<number | null>(null);
    // Snaps the page back to the right without animating it after the index changes.
    const [resetting, setResetting] = useState(false);
    const flipTimer = useRef<number | undefined>(undefined);

    useEffect(() => () => window.clearTimeout(flipTimer.current), []);

    const count = QUALITY_EXAMPLES.length;
    const flipping = target !== null;
    const current = QUALITY_EXAMPLES[index];
    const next = QUALITY_EXAMPLES[target ?? (index + 1) % count];

    const showWithoutAnimation = (newIndex: number) => {
        setIndex(newIndex);
        setTarget(null);
        setResetting(true);
        requestAnimationFrame(() => requestAnimationFrame(() => setResetting(false)));
    };

    const turnPageTo = (newIndex: number) => {
        if (flipping || newIndex === index) {
            return;
        }
        setTarget(newIndex);
        flipTimer.current = window.setTimeout(() => showWithoutAnimation(newIndex), FLIP_DURATION_MS);
    };

    const activeIndex = target ?? index;
    const pageClassName = [styles.Page, flipping ? styles.Flipping : "", resetting ? styles.Resetting : ""].join(" ");

    return (
        <section className={styles.MainWrapper} id="quality">
            <div className={`MainContainer ${styles.Inner}`}>
                <div className={styles.Heading}>
                    <div>
                        <SectionLabel index="02" label="Known issues detection" />
                        <h2 className={styles.Title}>Problematic code does not reach production.</h2>
                    </div>
                    <p className={styles.Description}>
                        Axelix knows the patterns that cause incidents in the Spring stack. It reads every layer of the
                        service, from code and configuration to dependencies, points to the exact line and stops the
                        release, whether a human or an AI agent wrote the code.
                    </p>
                </div>

                <div className={styles.Scroller}>
                    <div className={styles.Book}>
                        <div className={styles.Tabs}>
                            {QUALITY_EXAMPLES.map(({ tab, file }, tabIndex) => (
                                <button
                                    key={tab}
                                    type="button"
                                    aria-pressed={tabIndex === activeIndex}
                                    className={`${styles.Tab} ${tabIndex === activeIndex ? styles.TabActive : ""}`}
                                    onClick={() => turnPageTo(tabIndex)}
                                >
                                    <span>{tab}</span>
                                    <span className={styles.TabFile}>{file}</span>
                                </button>
                            ))}
                        </div>

                        <div className={styles.Spread}>
                            <FindingsPage example={current} />
                            <CodePage example={next} />
                            <div className={pageClassName}>
                                <div className={styles.Face}>
                                    <CodePage example={current} />
                                    <div className={styles.FrontShade} />
                                </div>
                                <div className={`${styles.Face} ${styles.Back}`}>
                                    <FindingsPage example={next} />
                                    <div className={styles.BackShade} />
                                </div>
                            </div>
                            <div className={styles.Spine} />
                        </div>

                        <div className={styles.Footer}>
                            <button
                                type="button"
                                className={styles.TurnButton}
                                onClick={() => turnPageTo((index + 1) % count)}
                            >
                                Turn the page →
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
};
