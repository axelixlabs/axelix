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
        <>
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
        </>
    );
};
