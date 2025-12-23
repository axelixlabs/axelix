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
import { useTranslation } from "react-i18next";

import type { IThread } from "models";

import { ThreadDumpLockInfo } from "../ThreadDumpLockInfo";
import { ThreadDumpRow } from "../ThreadDumpRow";
import { ThreadDumpStackTrace } from "../ThreadDumpStackTrace";

import styles from "./styles.module.css";

interface IProps {
    /**
     *  An object representing the thread dump.
     */
    threadDump: IThread;
}

export const ThreadDumpAccordionBody = ({ threadDump }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.AccordionBody}>
                <ThreadDumpRow title={t("ThreadDump.id")} value={threadDump.threadId} />
                <ThreadDumpRow title={t("ThreadDump.name")} value={threadDump.threadName} />
                <ThreadDumpRow title={t("ThreadDump.state")} value={threadDump.threadState} />
                <ThreadDumpRow title={t("ThreadDump.priority")} value={threadDump.priority} />
                <ThreadDumpRow title={t("ThreadDump.blockedCount")} value={threadDump.blockedCount} />
                <ThreadDumpRow title={t("ThreadDump.blockedTime")} value={threadDump.blockedTime} />
                <ThreadDumpRow title={t("ThreadDump.waitedCount")} value={threadDump.waitedCount} />
                <ThreadDumpRow title={t("ThreadDump.daemon")} value={String(threadDump.daemon)} />
                <ThreadDumpRow
                    title={t("ThreadDump.lockInfo")}
                    value={<ThreadDumpLockInfo threadDump={threadDump} />}
                />
            </div>
            <ThreadDumpStackTrace threadDump={threadDump} />
        </>
    );
};
