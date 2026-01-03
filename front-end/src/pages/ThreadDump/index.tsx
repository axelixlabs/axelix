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
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { Accordion, EmptyHandler, Loader } from "components";
import { fetchData, getDisplayedThreadDump } from "helpers";
import { type IThread, type IThreadDumpResponseBody, type IThreadGroup, StatefulRequest } from "models";
import { getThreadDumpData } from "services";
import { THREAD_DUMP_SHORT_POLLING_INTERVAL_MS } from "utils";

import { GlobalSlidingTimeLine } from "./GlobalSlidingTimeLine";
import { ThreadDumpAccordionBody } from "./ThreadDumpAccordionBody";
import { SingleThreadAccordionHeader } from "./ThreadDumpAccordionHeader";
import styles from "./styles.module.css";

const ThreadDump = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();

    const [threadDumpData, setThreadDumpData] = useState(StatefulRequest.loading<IThreadDumpResponseBody>());
    const [selectedGroups, setSelectedGroups] = useState<Record<string, IThreadGroup>>({});

    useEffect(() => {
        const doFetch = () => {
            fetchData(setThreadDumpData, () => getThreadDumpData(instanceId!));
        };

        doFetch();

        const intervalId = setInterval(doFetch, THREAD_DUMP_SHORT_POLLING_INTERVAL_MS);

        return () => clearInterval(intervalId);
    }, []);

    if (threadDumpData.loading) {
        return <Loader />;
    }

    if (threadDumpData.error) {
        return <EmptyHandler isEmpty />;
    }

    const threadDumpFeed = threadDumpData.response!.threads;
    const sortedThreadDump = threadDumpFeed.toSorted(
        (currentThread, nextThread) => nextThread.priority - currentThread.priority,
    );

    const onAccordionClose = (threadDump: IThread): void => {
        setSelectedGroups((prev) => {
            const prevGroups = { ...prev };
            delete prevGroups[threadDump.threadId];
            return prevGroups;
        });
    };

    return (
        <>
            {/* Empty attribute required for the correct styling to be applied, see MainLayout component styling */}
            <div data-thread-layout className={styles.TitleAndTimelineWrapper}>
                <div className={`TextMedium ${styles.MainTitle}`}>{t("ThreadDump.title")}</div>
                <GlobalSlidingTimeLine />
            </div>

            <div className={`AccordionsWrapper ${styles.AccordionsWrapper}`}>
                {sortedThreadDump.map((threadDump) => (
                    <Accordion
                        header={
                            <SingleThreadAccordionHeader
                                currentThreadSnapshot={threadDump}
                                selectedGroups={selectedGroups}
                                setSelectedGroups={setSelectedGroups}
                            />
                        }
                        key={threadDump.threadId}
                        onClose={() => onAccordionClose(threadDump)}
                        hideArrowIcon
                    >
                        <ThreadDumpAccordionBody thread={getDisplayedThreadDump(threadDump, selectedGroups)} />
                    </Accordion>
                ))}
            </div>
        </>
    );
};

export default ThreadDump;
