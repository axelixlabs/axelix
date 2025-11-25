import { Tree } from "antd";
import { useTranslation } from "react-i18next";

import type { IThread } from "models";

import styles from "./styles.module.css";

interface IProps {
    threadDump: IThread;
}

export const ThreadDumpLockInfo = ({ threadDump }: IProps) => {
    const { t } = useTranslation();

    const treeData = [
        {
            title: threadDump.lockName,
            key: "0-0",
            children: [
                {
                    title: (
                        <div className={styles.TreeItem}>
                            <div className={styles.TreeLabel}>{t("ThreadDump.className")}:</div>
                            <div>{threadDump.lockInfo.className}</div>
                        </div>
                    ),
                    key: "0-0-0",
                },
                {
                    title: (
                        <div className={styles.TreeItem}>
                            <div className={styles.TreeLabel}>{t("ThreadDump.lockOwnerId")}:</div>
                            <div>{threadDump.lockOwnerId}</div>
                        </div>
                    ),
                    key: "0-0-1",
                },
                {
                    title: (
                        <div className={styles.TreeItem}>
                            <div className={styles.TreeLabel}>{t("ThreadDump.lockOwnerName")}:</div>
                            <div>{threadDump.lockOwnerName}</div>
                        </div>
                    ),
                    key: "0-0-2",
                },
                {
                    title: (
                        <div className={styles.TreeItem}>
                            <div className={styles.TreeLabel}>{t("ThreadDump.identityHashCode")}:</div>
                            <div>{threadDump.lockInfo.identityHashCode}</div>
                        </div>
                    ),
                    key: "0-0-3",
                },
            ],
        },
    ];

    return <Tree expandAction="click" showLine treeData={treeData} className={styles.Tree} />;
};
