import {useEffect, useState} from 'react';
import {useParams} from 'react-router-dom';
import {useTranslation} from 'react-i18next';

import {
  isEmpty,
  type ScheduledTasksResponse,
  StatefulRequest,
} from 'models';

import {EmptyHandler, Loader, PageSearch} from "components";
import {fetchData, filterScheduledTasks} from 'helpers';

import {getScheduledTasksData} from "services";
import {CronTasks} from "./CronTasks";
import {FixedTasks} from "./FixedTasks";

const ScheduledTasks = () => {
    const { instanceId } = useParams()
    const { t } = useTranslation()

    const [scheduledTasks, setScheduledTasks] = useState(StatefulRequest.loading<ScheduledTasksResponse>());
    const [search, setSearch] = useState<string>("")

    const fetchScheduledTasks = (instanceId: string) => fetchData(
      setScheduledTasks,
      () => getScheduledTasksData(instanceId))

    useEffect(() => {
        if (instanceId) {
            fetchScheduledTasks(instanceId);
        }
    }, [])

    if (scheduledTasks.loading) {
        return <Loader />
    }

    if (scheduledTasks.error) {
        return scheduledTasks.error
    }

    const scheduledTasksData = scheduledTasks.response!;

    const effectiveScheduledTasks = search
      ? filterScheduledTasks(scheduledTasksData, search)
      : scheduledTasksData
    return (
        <>
            <PageSearch onChange={(e) => setSearch(e)} />

            <EmptyHandler isEmpty={isEmpty(effectiveScheduledTasks)}>
              <CronTasks cronTasks={effectiveScheduledTasks.cron}/>
              <FixedTasks taskTitle={t("ScheduledTasks.fixedDelay")} fixedTasks={effectiveScheduledTasks.fixedDelay}/>
              <FixedTasks taskTitle={t("ScheduledTasks.fixedRate")} fixedTasks={effectiveScheduledTasks.fixedRate}/>
            </EmptyHandler>
        </>
    )
};

export default ScheduledTasks