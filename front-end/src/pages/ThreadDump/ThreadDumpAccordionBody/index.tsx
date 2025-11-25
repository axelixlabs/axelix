import { useTranslation } from "react-i18next";

import type { IThread } from "models";

import { ThreadDumpLockInfo } from "../ThreadDumpLockInfo";
import { ThreadDumpRow } from "../ThreadDumpRow";

import styles from "./styles.module.css";

interface IProps {
    threadDump: IThread;
}

export const ThreadDumpAccordionBody = ({ threadDump }: IProps) => {
    const { t } = useTranslation();

    return (
        <div className={styles.AccordionBody}>
            <ThreadDumpRow title={t("ThreadDump.id")} value={threadDump.threadId} />
            <ThreadDumpRow title={t("ThreadDump.name")} value={threadDump.threadName} />
            <ThreadDumpRow title={t("ThreadDump.priority")} value={String(threadDump.priority)} />
            <ThreadDumpRow title={t("ThreadDump.state")} value={threadDump.threadState} />
            <ThreadDumpRow title={t("ThreadDump.blockedCount")} value={threadDump.blockedCount} />
            <ThreadDumpRow title={t("ThreadDump.blockedTime")} value={threadDump.blockedTime} />
            <ThreadDumpRow title={t("ThreadDump.waitedCount")} value={threadDump.waitedCount} />
            {threadDump.lockName && <ThreadDumpRow title={t("ThreadDump.lockName")} value={threadDump.lockName} />}
            <ThreadDumpRow title={t("ThreadDump.lockOwnerId")} value={threadDump.lockOwnerId} />
            {threadDump.lockOwnerName && (
                <ThreadDumpRow title={t("ThreadDump.lockOwnerName")} value={threadDump.lockOwnerName} />
            )}
            <ThreadDumpRow title={t("ThreadDump.daemon")} value={String(threadDump.daemon)} />
            {threadDump.lockInfo && (
                <ThreadDumpRow
                    title={t("ThreadDump.lockInfo")}
                    value={<ThreadDumpLockInfo threadDump={threadDump} />}
                />
            )}
        </div>
    );
};
