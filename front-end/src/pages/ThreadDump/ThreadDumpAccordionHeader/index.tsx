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
import { THREAD_DUMP_SLIDING_WINDOW_MS, threadDumpStateLetters } from "utils";

import { ThreadTimeLine } from "../ThreadTimeLine";

import styles from "./styles.module.css";

interface IProps {
    /**
     *  An object representing the current thread dump snapshot.
     */
    currentThreadSnapshot: IThread;

    /**
     * Map of selected thread groups. Keys are thread ids, values are
     * the selected groups for the given threads.
     */
    selectedGroups: Record<string, IThreadGroup>;

    /**
     * Setter to update the selected thread groups
     */
    setSelectedGroups: Dispatch<SetStateAction<Record<string, IThreadGroup>>>;
}

export const SingleThreadAccordionHeader = ({ currentThreadSnapshot, selectedGroups, setSelectedGroups }: IProps) => {
    const [history, setHistory] = useState<IThread[]>([]);

    useEffect(() => {
        const id = setInterval(() => {
            setHistory([]);
            setSelectedGroups({});
        }, THREAD_DUMP_SLIDING_WINDOW_MS);

        return () => clearInterval(id);
    }, []);

    useEffect(() => {
        setHistory((prev) => [...prev, currentThreadSnapshot]);
    }, [currentThreadSnapshot]);

    const { colorPrimary } = getThreadStateColor(currentThreadSnapshot);

    return (
        <div className={styles.MainWrapper}>
            <div
                className={styles.ThreadNameAvatar}
                style={{
                    backgroundColor: colorPrimary,
                }}
            >
                {threadDumpStateLetters[currentThreadSnapshot.threadState]}
            </div>
            <div>{currentThreadSnapshot.threadName}</div>

            <ThreadTimeLine history={history} selectedGroups={selectedGroups} setSelectedGroups={setSelectedGroups} />
        </div>
    );
};
