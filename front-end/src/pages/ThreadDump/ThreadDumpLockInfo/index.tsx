/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
                    <div className={styles.ContentLabel}>{t("ThreadDump.lock")}:</div>
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
            <div className={styles.ContentLabel}>{t("ThreadDump.lock")}:</div>
        </div>
    );
};
