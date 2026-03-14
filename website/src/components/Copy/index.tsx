"use client"
import { useState } from "react";

import { CopyIcon, CopiedIcon } from "@/assets"

import styles from "./styles.module.css";
import { Tooltip } from "antd";

interface IProps {
    text: string;
}

export const Copy = ({ text }: IProps) => {
    const [copied, setCopied] = useState<boolean>(false);

    const handleCopy = async (): Promise<void> => {
        try {
            await navigator.clipboard.writeText(text);
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
        } catch (err) {
            console.error('Failed to copy!', err);
        }
    };

    return (
        <>
            {
                copied
                    ? <>
                        <Tooltip open={true} trigger={[]} title="Copied!">
                            <CopiedIcon />
                        </Tooltip>
                    </>
                    : <CopyIcon className={styles.Copy} onClick={handleCopy} />
            }
        </>
    )
} 