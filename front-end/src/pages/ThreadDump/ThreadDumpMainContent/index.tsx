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
import { useEffect, useState } from "react";

import { Accordion } from "components";
import { getDisplayedThreadDump, getThreadHistoryNextState } from "helpers";
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
        setThreadsHistory((prev) => getThreadHistoryNextState(prev, sortedThreadDump));
    }, [sortedThreadDump]);

    const onAccordionClose = (threadDump: IThread): void => {
        setSelectedGroups((prev) => {
            const prevGroups = { ...prev };
            delete prevGroups[threadDump.threadId];
            return prevGroups;
        });
    };

    return (
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
    );
};
