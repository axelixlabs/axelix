// artifact: string
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
