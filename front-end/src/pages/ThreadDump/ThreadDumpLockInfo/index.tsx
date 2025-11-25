import { Tree } from "antd";
import { useTranslation } from "react-i18next";

import type { IThread } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     *  An object representing the thread dump.
     */
    threadDump: IThread;
}

export const ThreadDumpLockInfo = ({ threadDump }: IProps) => {
    const { t } = useTranslation();

    const treeData = [
        {
            title: threadDump.lockName,
            key: threadDump.threadId,
            children: [
                {
                    title: (
                        <div className={styles.TreeItem}>
                            <div className={styles.TreeLabel}>{t("ThreadDump.className")}:</div>
                            <div>{threadDump.lockInfo.className}</div>
                        </div>
                    ),
                    key: `${threadDump.threadId} ${threadDump.lockInfo.className}`,
                },
                {
                    title: (
                        <div className={styles.TreeItem}>
                            <div className={styles.TreeLabel}>{t("ThreadDump.lockOwnerId")}:</div>
                            <div>{threadDump.lockOwnerId}</div>
                        </div>
                    ),
                    key: `${threadDump.threadId} ${threadDump.lockOwnerId}`,
                },
                {
                    title: (
                        <div className={styles.TreeItem}>
                            <div className={styles.TreeLabel}>{t("ThreadDump.lockOwnerName")}:</div>
                            <div>{threadDump.lockOwnerName}</div>
                        </div>
                    ),
                    key: `${threadDump.threadId} ${threadDump.lockOwnerName}`,
                },
                {
                    title: (
                        <div className={styles.TreeItem}>
                            <div className={styles.TreeLabel}>{t("ThreadDump.identityHashCode")}:</div>
                            <div>{threadDump.lockInfo.identityHashCode}</div>
                        </div>
                    ),
                    key: `${threadDump.threadId} ${threadDump.lockInfo.identityHashCode}`,
                },
            ],
        },
    ];

    return <Tree expandAction="click" showLine treeData={treeData} className={styles.Tree} />;
};
