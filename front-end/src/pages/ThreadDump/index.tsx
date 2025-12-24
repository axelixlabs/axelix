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
import { fetchData } from "helpers";
import { type IThreadDumpResponseBody, StatefulRequest } from "models";
import { getThreadDumpData } from "services";

import { ThreadDumpAccordionBody } from "./ThreadDumpAccordionBody";
import { ThreadDumpAccordionHeader } from "./ThreadDumpAccordionHeader";
import { ThreadDumpTimeLine } from "./ThreadDumpTimeLine";
import styles from "./styles.module.css";

const ThreadDump = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();

    const [threadDumpData, setThreadDumpData] = useState(StatefulRequest.loading<IThreadDumpResponseBody>());

    useEffect(() => {
        const doFetch = () => {
            fetchData(setThreadDumpData, () => getThreadDumpData(instanceId!));
        };

        doFetch();

        const intervalId = setInterval(doFetch, 1000);

        return () => clearInterval(intervalId);
    }, []);

    if (threadDumpData.loading) {
        return <Loader />;
    }

    if (threadDumpData.error) {
        return <EmptyHandler isEmpty />;
    }

    const threadDumpFeed = threadDumpData.response!.threads;
    const sortedThreadDump = threadDumpFeed.sort(
        (currentThread, nextThread) => nextThread.priority - currentThread.priority,
    );

    return (
        <>
            {/* Empty attribute required for the correct styling to be applied un MainLayout */}
            <div data-thread-layout className={styles.TitleAndTimelineWrapper}>
                <div className={`TextMedium ${styles.MainTitle}`}>{t("ThreadDump.title")}</div>
                <ThreadDumpTimeLine />
            </div>

            <div className="AccordionsWrapper">
                {sortedThreadDump.map((threadDump) => (
                    <Accordion
                        header={<ThreadDumpAccordionHeader threadDump={threadDump} />}
                        key={threadDump.threadId}
                        hideArrowIcon
                    >
                        <ThreadDumpAccordionBody thread={threadDump} />
                    </Accordion>
                ))}
            </div>
        </>
    );
};

export default ThreadDump;
