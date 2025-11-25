import type { ReactNode } from "react";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The title of the row.
     */
    title: string;

    /**
     * The content or value.
     * Can be any valid React node.
     */
    value: ReactNode;
}

export const ThreadDumpRow = ({ title, value }: IProps) => {
    return (
        <>
            <div className={styles.AccordionBodyChunkTitle}>{title}:</div>
            <div>{value}</div>
        </>
    );
};
