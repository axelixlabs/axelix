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
                // TODO:
                //  Now, the contention monitoring component property is drilled down really
                //  hard. I think this is going to be a good case for using the state manager here later.
                contentionMonitoring={contentionMonitoring}
            />

            <ThreadDumpMainContent sortedThreadDump={sortedThreadDump} />
        </>
    );
};

export default ThreadDump;
