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
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader } from "components";
import { fetchData, filterThreadDump, sortThreadDumpByPriority } from "helpers";
import { type IThreadDumpResponseBody, StatefulRequest } from "models";
import { getThreadDumpData } from "services";
import { THREAD_DUMP_SHORT_POLLING_INTERVAL_MS } from "utils";

import { ThreadDumpFirstSection } from "./ThreadDumpFirstSection";
import { ThreadDumpMainContent } from "./ThreadDumpMainContent";

const ThreadDump = () => {
    const { instanceId } = useParams();

    const [threadDumpData, setThreadDumpData] = useState(StatefulRequest.loading<IThreadDumpResponseBody>());
    const [search, setSearch] = useState<string>("");

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

    const contentionMonitoring = threadDumpData.response!.threadContentionMonitoringEnabled;
    const threadDumpFeed = threadDumpData.response!.threads;
    const effectiveThreadDump = search ? filterThreadDump(threadDumpFeed, search) : threadDumpFeed;
    const sortedThreadDump = sortThreadDumpByPriority(effectiveThreadDump);
    const addonAfter = `${effectiveThreadDump.length} / ${threadDumpFeed.length}`;

    return (
        <>
            <ThreadDumpFirstSection
                setSearch={setSearch}
                addonAfter={addonAfter}
                contentionMonitoring={contentionMonitoring}
            />

            <ThreadDumpMainContent sortedThreadDump={sortedThreadDump} />
        </>
    );
};

export default ThreadDump;
