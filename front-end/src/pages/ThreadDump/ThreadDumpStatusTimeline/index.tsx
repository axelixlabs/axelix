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

import { generateThreadGroups, getThreadStateColor, stopPropagationOnAccordionExpand } from "helpers";
import type { IThread, IThreadGroup } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * An array of thread snapshots representing the thread's history.
     */
    history: IThread[];

    /**
     * Map of selected thread groups
     */
    selectedGroups: Record<string, IThreadGroup>;

    /**
     * Setter to update the selected thread groups
     */
    setSelectedGroups: Dispatch<SetStateAction<Record<string, IThreadGroup>>>;
}

export const ThreadDumpTimeline = ({ history, selectedGroups, setSelectedGroups }: IProps) => {
    const threadGroups = generateThreadGroups(history);

    return (
        <div className={styles.MainWrapper}>
            {threadGroups.map((threadGroup) => {
                const { id, count, thread } = threadGroup;
                const threadId = String(thread.threadId);

                const isGroupSelected = selectedGroups[threadId]?.id === id;

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
