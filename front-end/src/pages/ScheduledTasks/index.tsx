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
import { CronTasks } from "./Cron/CronTasks";
import { FixedTasks } from "./FixedTasks/FixedTask";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader, PageSearch } from "components";
import { fetchData, filterScheduledTasks, isEmpty } from "helpers";
import { type IScheduledTasksResponseBody, StatefulRequest } from "models";
import { getScheduledTasksData } from "services";

const ScheduledTasks = () => {
    const { instanceId } = useParams();
    const { t } = useTranslation();

    const [scheduledTasks, setScheduledTasks] = useState(StatefulRequest.loading<IScheduledTasksResponseBody>());
    const [search, setSearch] = useState<string>("");

    const fetchScheduledTasks = (instanceId: string) =>
        fetchData(setScheduledTasks, () => getScheduledTasksData(instanceId));

    useEffect(() => {
        fetchScheduledTasks(instanceId!);
    }, []);

    if (scheduledTasks.loading) {
        return <Loader />;
    }

    if (scheduledTasks.error) {
        return <EmptyHandler isEmpty />;
    }

    const scheduledTasksData = scheduledTasks.response!;

    const effectiveScheduledTasks = search ? filterScheduledTasks(scheduledTasksData, search) : scheduledTasksData;
    return (
        <>
            <PageSearch setSearch={setSearch} />

            <EmptyHandler isEmpty={isEmpty(effectiveScheduledTasks)}>
                <CronTasks cronTasks={effectiveScheduledTasks.cron} />
                <FixedTasks
                    taskTitle={t("ScheduledTasks.fixedDelay")}
                    fixedTasks={effectiveScheduledTasks.fixedDelay}
                />
                <FixedTasks taskTitle={t("ScheduledTasks.fixedRate")} fixedTasks={effectiveScheduledTasks.fixedRate} />
            </EmptyHandler>
        </>
    );
};

export default ScheduledTasks;
