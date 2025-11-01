import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader, PageSearch } from "components";
import { fetchData, filterScheduledTasks, isEmpty } from "helpers";
import { type IScheduledTasksResponseBody, StatefulRequest } from "models";
import { getScheduledTasksData } from "services";

import { CronTasks } from "./CronTasks";
import { FixedTasks } from "./FixedTasks";

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
        return scheduledTasks.error;
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
