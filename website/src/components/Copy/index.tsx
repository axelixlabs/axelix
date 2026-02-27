"use client"
import Image from "next/image"
import { useState } from "react";

import CopyIcon from "@/assets/icons/copy.svg"
import CopiedIcon from "@/assets/icons/ok.svg"

import styles from "./styles.module.css";
import {Tooltip} from "antd";

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
                            <Image src={CopiedIcon} alt="Copied icon" />
                        </Tooltip>
                    </>
                    : <Image className={styles.Copy} src={CopyIcon} alt="Copy icon" onClick={handleCopy} />
            }
        </>
    )
} 