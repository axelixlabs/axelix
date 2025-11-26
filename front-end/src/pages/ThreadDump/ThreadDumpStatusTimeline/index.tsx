import { threadStateColor } from "helpers";
import { EThreadDumpStateColors, type IThread } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * An array of thread snapshots representing the thread's history.
     */
    history: IThread[];
}

export const ThreadDumpTimeline = ({ history }: IProps) => {
    return (
        <div className={styles.MainWrapper}>
            {history.map((singleHistory, index) => (
                <div
                    className={`${styles.SingleHistoryChunk} ${styles[threadStateColor(singleHistory) as EThreadDumpStateColors]}`}
                    key={index}
                />
            ))}
        </div>
    );
};
