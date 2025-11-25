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
            <div className={styles.TitleAndTimelineWrapper}>
                <div className={`MediumTitle ${styles.MainTitle}`}>{t("ThreadDump.title")}</div>
                <ThreadDumpTimeLine />
            </div>

            <div className="AccordionsWrapper">
                {sortedThreadDump.map((threadDump) => (
                    <Accordion
                        header={<ThreadDumpAccordionHeader threadDump={threadDump} />}
                        key={threadDump.threadId}
                        hideArrowIcon
                    >
                        <ThreadDumpAccordionBody threadDump={threadDump} />
                    </Accordion>
                ))}
            </div>
        </>
    );
};

export default ThreadDump;
