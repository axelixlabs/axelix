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
import { useEffect, useState } from "react";

import { Accordion } from "components";
import { appendToThreadDumpHistory, getDisplayedThreadDump } from "helpers";
import type { IThread, IThreadGroup } from "models";
import { THREAD_DUMP_SLIDING_WINDOW_MS } from "utils";

import { SingleThreadAccordionHeader } from "../SingleThreadAccordionHeader";
import { ThreadDumpAccordionBody } from "../ThreadDumpAccordionBody";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Thread dump sorted by priority
     */
    sortedThreadDump: IThread[];
}

export const ThreadDumpMainContent = ({ sortedThreadDump }: IProps) => {
    const [selectedGroups, setSelectedGroups] = useState<Record<string, IThreadGroup>>({});
    const [threadsHistory, setThreadsHistory] = useState<Record<string, IThread[]>>({});

    useEffect(() => {
        const id = setInterval(() => {
            setThreadsHistory({});
            setSelectedGroups({});
        }, THREAD_DUMP_SLIDING_WINDOW_MS);

        return () => clearInterval(id);
    }, []);

    useEffect(() => {
        setThreadsHistory((prev) => appendToThreadDumpHistory(prev, sortedThreadDump));
    }, [sortedThreadDump]);

    const onAccordionClose = (threadDump: IThread): void => {
        setSelectedGroups((prev) => {
            const prevGroups = { ...prev };
            delete prevGroups[threadDump.threadId];
            return prevGroups;
        });
    };

    return (
        <>
            <div className={`AccordionsWrapper ${styles.AccordionsWrapper}`}>
                {sortedThreadDump.map((threadDump) => {
                    const history = threadsHistory[threadDump.threadId] || [];
                    const currentSelectedGroup = selectedGroups[threadDump.threadId];

                    return (
                        <Accordion
                            header={
                                <SingleThreadAccordionHeader
                                    currentThreadSnapshot={threadDump}
                                    selectedGroup={currentSelectedGroup}
                                    setSelectedGroups={setSelectedGroups}
                                    history={history}
                                />
                            }
                            key={threadDump.threadId}
                            onClose={() => onAccordionClose(threadDump)}
                            hideArrowIcon
                            accordionExpanded={!!currentSelectedGroup}
                        >
                            <ThreadDumpAccordionBody thread={getDisplayedThreadDump(threadDump, selectedGroups)} />
                        </Accordion>
                    );
                })}
            </div>
        </>
    );
};
