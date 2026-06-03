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
import { CopyIcon } from "@/assets";

import { useRef, useState } from "react";

import styles from "./styles.module.css";

interface IProps {
    artifact: string;
    activeSnippetRef: any;
}

export const StarterMini = ({ artifact, activeSnippetRef }: IProps) => {
    const [copiedIdx, setCopiedIdx] = useState<number | null>(null);
    const timers = useRef<Array<ReturnType<typeof setTimeout> | null>>([null, null, null]);

    const axelixActualVersion = `1.0.0-M1`;

    const blocks: { label: string; code: string }[] = [
        {
            label: "Gradle Kotlin DSL",
            code: `implementation("com.axelixlabs:${artifact}:${axelixActualVersion}")`,
        },
        {
            label: "Gradle Groovy DSL",
            code: `implementation 'com.axelixlabs:${artifact}:${axelixActualVersion}'`,
        },
        {
            label: "Maven",
            code: `<dependency>
    <groupId>com.axelixlabs</groupId>
    <artifactId>${artifact}</artifactId>
    <version>${axelixActualVersion}</version>
</dependency>`,
        },
    ];

    async function copy(idx: number, text: string) {
        try {
            await navigator.clipboard.writeText(text);
            setCopiedIdx(idx);
            if (timers.current[idx]) clearTimeout(timers.current[idx]!);
            timers.current[idx] = setTimeout(() => setCopiedIdx(null), 1200);
        } catch {
            /* clipboard blocked */
        }
    }

    return (
        <>
            <div
                className={`${styles.Snippet} ${styles.Active}`}
                ref={(el) => {
                    activeSnippetRef.current = el;
                }}
            >
                {blocks.map((b, i) => (
                    <div key={b.label} className={styles.Mini}>
                        <div className={styles.MiniHead}>
                            <span className={styles.MiniLabel}>{b.label}</span>
                            <button type="button" className={styles.MiniCopy} onClick={() => copy(i, b.code)}>
                                {copiedIdx === i ? (
                                    <span>Copied</span>
                                ) : (
                                    <>
                                        <CopyIcon />
                                        Copy
                                    </>
                                )}
                            </button>
                        </div>
                        <pre className={styles.MiniCode}>{b.code}</pre>
                    </div>
                ))}
            </div>
        </>
    );
};
