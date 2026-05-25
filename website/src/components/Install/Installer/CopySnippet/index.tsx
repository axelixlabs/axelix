import { useRef, useState } from "react";
import styles from "./styles.module.css"
import { CopyIcon } from "@/assets";

interface IProps {
    activeSnippetRef: any
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
        <button
            className={styles.CopyFloat}
            type="button"
            title="Copy"
            onClick={copyMain}
        >
            {copied ? <span>Copied</span> : (
                <>
                    <CopyIcon />
                    <span>Copy</span>
                </>
            )}
        </button>
    )
}