import { threadStateColor } from "helpers";
import { type IThread } from "models";

import styles from "./styles.module.css";

interface IProps {
    history: IThread[];
}

export const ThreadDumpTimeline = ({ history }: IProps) => {
    return (
        <div className={styles.MainWrapper}>
            {history.map((singleHistory) => (
                <div className={`${styles.SingleHistoryChunk} ${styles[threadStateColor(singleHistory)]}`} />
            ))}
        </div>
    );
};
