/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
        <>
            <Tree expandAction="click" showLine treeData={treeData} className={styles.Tree} />
        </>
    ) : (
        <>
            {" "}
            <div className={styles.ContentWrapper}>
                <div className={styles.ContentLabel}>{t("ThreadDump.lock")}:</div>
            </div>
        </>
    );
};
