import { CheckIcon, CopyIcon } from "@/assets";

import { useRef, useState } from "react";

import styles from "./styles.module.css";

interface IProps {
    activeSnippetRef: any;
}

export const CopySnippet = ({ activeSnippetRef }: IProps) => {
    const [copied, setCopied] = useState(false);
    const copyTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

    async function copyMain() {
        const el = activeSnippetRef.current;
        if (!el) return;
        const text = (el.innerText || "").replace(/ /g, " ");
        try {
            await navigator.clipboard.writeText(text);
            setCopied(true);
            if (copyTimer.current) clearTimeout(copyTimer.current);
            copyTimer.current = setTimeout(() => setCopied(false), 1200);
        } catch {
            /* clipboard blocked */
        }
    }

    return (
        <div className={styles.MainWrapper}>
            <button className={styles.CopyFloat} type="button" title="Copy" onClick={copyMain}>
                {copied ? (
                    <>
                        <CheckIcon />
                        <span>Copied!</span>
                    </>
                ) : (
                    <>
                        <CopyIcon />
                        <span>Copy</span>
                    </>
                )}
            </button>
        </div>
    );
};
