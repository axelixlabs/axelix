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

import { getThreadStateColor, partitionToThreadGroups, stopPropagationOnAccordionExpand } from "helpers";
import type { IThread, IThreadGroup } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * An array of thread snapshots representing the thread's history.
     */
    history: IThread[];

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

export const ThreadTimeLine = ({ history, selectedGroups, setSelectedGroups }: IProps) => {
    const threadGroups = partitionToThreadGroups(history);

    return (
        <div className={styles.MainWrapper}>
            {threadGroups.map((threadGroup) => {
                const { id, count, thread } = threadGroup;

                const isGroupSelected = selectedGroups[String(thread.threadId)]?.id === id;

                const color = getThreadStateColor(thread);

                return (
                    <div
                        key={id}
                        className={`${styles.ThreadGroup} ${isGroupSelected ? styles.SelectedThreadGroup : ""}`}
                        style={
                            {
                                width: `${5 * count}px`,
                                "--color-primary": color.colorPrimary,
                                "--color-primary-hover": color.colorPrimaryHover,
                                "--color-primary-active": color.colorPrimaryActive,
                            } as React.CSSProperties
                        }
                        onClick={(e) => {
                            stopPropagationOnAccordionExpand(e);

                            // overwriting the previous selected group for this thread.
                            setSelectedGroups((prev) => ({
                                ...prev,
                                [String(thread.threadId)]: threadGroup,
                            }));
                        }}
                    />
                );
            })}
        </div>
    );
};
