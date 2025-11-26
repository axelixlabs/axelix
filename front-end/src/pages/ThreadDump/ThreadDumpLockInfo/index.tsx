import { Tree, type TreeDataNode } from "antd";
import { useTranslation } from "react-i18next";

import type { IThread } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * An object representing the thread dump.
     */
    threadDump: IThread;
}

export const ThreadDumpLockInfo = ({ threadDump }: IProps) => {
    const { t } = useTranslation();

    const children: TreeDataNode[] = [];

    if (threadDump.lockName) {
        children.push({
            title: (
                <div className={styles.ContentWrapper}>
                    <div className={styles.ContentLabel}>{t("ThreadDump.lockName")}:</div>
                    <div>{threadDump.lockName}</div>
                </div>
            ),
            key: `${threadDump.threadId}-lockName`,
        });
    }

    if (threadDump.lockOwnerName) {
        children.push({
            title: (
                <div className={styles.ContentWrapper}>
                    <div className={styles.ContentLabel}>{t("ThreadDump.lockOwnerName")}:</div>
                    <div>{threadDump.lockOwnerName}</div>
                </div>
            ),
            key: `${threadDump.threadId}-lockOwnerName`,
        });
    }

    if (threadDump.lockInfo) {
        children.push({
            title: (
                <div className={styles.ContentWrapper}>
                    <div className={styles.ContentLabel}>{t("ThreadDump.className")}:</div>
                    <div>{threadDump.lockInfo.className}</div>
                </div>
            ),
            key: `${threadDump.threadId}-className`,
        });

        if (threadDump.lockInfo.identityHashCode) {
            children.push({
                title: (
                    <div className={styles.ContentWrapper}>
                        <div className={styles.ContentLabel}>{t("ThreadDump.identityHashCode")}:</div>
                        <div>{threadDump.lockInfo.identityHashCode}</div>
                    </div>
                ),
                key: `${threadDump.threadId}-identityHashCode`,
            });
        }
    }

    const treeData: TreeDataNode[] = [
        {
            title: (
                <div className={styles.ContentWrapper}>
                    <div className={styles.ContentLabel}>{t("ThreadDump.lockOwnerId")}:</div>
                    <div>{threadDump.lockOwnerId}</div>
                </div>
            ),
            key: `${threadDump.threadId}-root`,
            children,
        },
    ];

    return children.length ? (
        <Tree expandAction="click" showLine treeData={treeData} className={styles.Tree} />
    ) : (
        <div className={styles.ContentWrapper}>
            <div className={styles.ContentLabel}>{t("ThreadDump.lockOwnerId")}:</div>
            <div>{threadDump.lockOwnerId}</div>
        </div>
    );
};
