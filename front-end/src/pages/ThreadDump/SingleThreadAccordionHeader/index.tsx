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
import type { Dispatch, SetStateAction } from "react";

import { getThreadStateColor } from "helpers";
import type { IThread, IThreadGroup } from "models";
import { threadDumpStateLetters } from "utils";

import { ThreadTimeLine } from "../ThreadTimeLine";

import styles from "./styles.module.css";

interface IProps {
    /**
     *  An object representing the current thread dump snapshot.
     */
    currentThreadSnapshot: IThread;

    /**
     * Selected thread group for this thread
     */
    selectedGroup: IThreadGroup | undefined;

    /**
     * Setter to update the selected thread groups
     */
    setSelectedGroups: Dispatch<SetStateAction<Record<string, IThreadGroup>>>;

    /**
     * The list of thread dump history
     */
    history: IThread[];
}

export const SingleThreadAccordionHeader = ({
    currentThreadSnapshot,
    selectedGroup,
    setSelectedGroups,
    history,
}: IProps) => {
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

            <ThreadTimeLine history={history} selectedGroup={selectedGroup} setSelectedGroups={setSelectedGroups} />
        </div>
    );
};
