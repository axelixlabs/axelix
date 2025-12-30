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
import { type Dispatch, type SetStateAction, useEffect, useState } from "react";

import { getThreadStateColor } from "helpers";
import type { IThread, IThreadGroup } from "models";
import { THREAD_DUMP_INTERVAL_MS, threadDumpStateLetters } from "utils";

import { ThreadDumpTimeline } from "../ThreadDumpStatusTimeline";

import styles from "./styles.module.css";

interface IProps {
    /**
     *  An object representing the thread dump.
     */
    threadDump: IThread;

    /**
     * Map of selected thread groups
     */
    selectedGroups: Record<string, IThreadGroup>;

    /**
     * Setter to update the selected thread groups
     */
    setSelectedGroups: Dispatch<SetStateAction<Record<string, IThreadGroup>>>;
}

export const ThreadDumpAccordionHeader = ({ threadDump, selectedGroups, setSelectedGroups }: IProps) => {
    const [history, setHistory] = useState<IThread[]>([]);

    useEffect(() => {
        const id = setInterval(() => {
            setHistory([]);
            setSelectedGroups({});
        }, THREAD_DUMP_INTERVAL_MS);

        return () => clearInterval(id);
    }, []);

    useEffect(() => {
        setHistory((prev) => [...prev, threadDump]);
    }, [threadDump]);

    const { colorPrimary } = getThreadStateColor(threadDump);

    return (
        <div className={styles.MainWrapper}>
            <div
                className={styles.ThreadNameAvatar}
                style={{
                    backgroundColor: colorPrimary,
                }}
            >
                {threadDumpStateLetters[threadDump.threadState]}
            </div>
            <div>{threadDump.threadName}</div>

            <ThreadDumpTimeline
                history={history}
                selectedGroups={selectedGroups}
                setSelectedGroups={setSelectedGroups}
            />
        </div>
    );
};
