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

    useEffect(() => {
        fetchData(setScheduledTasks, () => getScheduledTasksData(instanceId!));
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
