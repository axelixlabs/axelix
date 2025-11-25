import type { ReactNode } from "react";

import styles from "./styles.module.css";

interface IProps {
    title: string;
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
