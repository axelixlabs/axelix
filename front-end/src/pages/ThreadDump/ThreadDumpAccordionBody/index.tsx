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
import { useTranslation } from "react-i18next";

import type { IThread } from "models";

import { ThreadDumpLockInfo } from "../ThreadDumpLockInfo";
import { ThreadDumpRow } from "../ThreadDumpRow";
import ThreadDumpTrackableValue from "../ThreadDumpRow/ThreadDumpRowTooltipValue";
import { ThreadDumpStackTrace } from "../ThreadDumpStackTrace";

import styles from "./styles.module.css";

interface IProps {
    /**
     *  An object representing the thread dump.
     */
    thread: IThread;
}

export const ThreadDumpAccordionBody = ({ thread }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.AccordionBody}>
                <ThreadDumpRow title={t("ThreadDump.id")} value={thread.threadId} />
                <ThreadDumpRow title={t("ThreadDump.name")} value={thread.threadName} />
                <ThreadDumpRow title={t("ThreadDump.state")} value={thread.threadState} />
                <ThreadDumpRow title={t("ThreadDump.priority")} value={thread.priority} />
                <ThreadDumpRow title={t("ThreadDump.blockedCount")} value={thread.blockedCount} />
                <ThreadDumpRow
                    title={t("ThreadDump.blockedTime")}
                    value={<ThreadDumpTrackableValue value={thread.blockedTime} parameterName={"blockedTime"} />}
                />
                <ThreadDumpRow title={t("ThreadDump.waitedCount")} value={thread.waitedCount} />
                <ThreadDumpRow
                    title={t("ThreadDump.waitedTime")}
                    value={<ThreadDumpTrackableValue value={thread.waitedTime} parameterName={"waitedTime"} />}
                />

                <ThreadDumpRow title={t("ThreadDump.daemon")} value={String(thread.daemon)} />
                {thread.lockInfo && (
                    <ThreadDumpRow
                        title={t("ThreadDump.lockInfo")}
                        value={<ThreadDumpLockInfo threadDump={thread} />}
                    />
                )}
                {/* TODO: Add thread lock owner tree similar to lockInfo above*/}
            </div>
            <ThreadDumpStackTrace threadDump={thread} />
        </>
    );
};
