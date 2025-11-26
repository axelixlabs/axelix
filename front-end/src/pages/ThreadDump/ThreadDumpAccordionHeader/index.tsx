import { useEffect, useState } from "react";

import type { IThread } from "models";
import { TEN_MINUTES_MILLISECDONDS, threadDumpStateLetters } from "utils";

import { ThreadDumpTimeline } from "../ThreadDumpStatusTimeline";

import styles from "./styles.module.css";

interface IProps {
    /**
     *  An object representing the thread dump.
     */
    threadDump: IThread;
}

export const ThreadDumpAccordionHeader = ({ threadDump }: IProps) => {
    const [history, setHistory] = useState<IThread[]>([]);

    useEffect(() => {
        const id = setInterval(() => {
            setHistory([]);
        }, TEN_MINUTES_MILLISECDONDS);

        return () => clearInterval(id);
    }, []);

    useEffect(() => {
        setHistory((prev) => [...prev, threadDump]);
    }, [threadDump]);

    return (
        <div className={styles.MainWrapper}>
            {/* TODO: Add tooltip in future */}
            <div className={`${styles.ThreadNameAvatar} ${styles[threadDump.threadState]}`}>
                {threadDumpStateLetters[threadDump.threadState]}
            </div>
            <div>{threadDump.threadName}</div>
            <ThreadDumpTimeline history={history} />
        </div>
    );
};
