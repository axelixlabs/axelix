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
     * Selected thread group for this thread
     */
    selectedGroup: IThreadGroup | undefined;

    /**
     * Setter to update the selected thread groups
     */
    setSelectedGroups: Dispatch<SetStateAction<Record<string, IThreadGroup>>>;
}

export const ThreadTimeLine = ({ history, selectedGroup, setSelectedGroups }: IProps) => {
    const threadGroups = partitionToThreadGroups(history);

    return (
        <>
            <div className={styles.MainWrapper}>
                {threadGroups.map((threadGroup) => {
                    const { id, count, thread } = threadGroup;

                    const isGroupSelected = selectedGroup?.id === id;

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
        </>
    );
};
