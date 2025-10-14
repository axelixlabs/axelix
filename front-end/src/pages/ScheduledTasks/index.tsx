import { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

import { EmptyHandler, Loader, PageSearch } from "components";
import type { ICron, IFixedTasks, TaskKey } from 'models';
import { useAppDispatch, useAppSelector } from 'hooks';
import { getScheduledTasksThunk } from 'store/thunks';
import { filterScheduledTasks } from 'store/slices';
import { getScheduledTasksTypes } from 'utils';
import { TableHeader } from './TableHeader';
import { TableRow } from './TableRow';

import styles from './styles.module.css'

const ScheduledTasks = () => {
    const dispatch = useAppDispatch()
    const { instanceId } = useParams()
    const { t } = useTranslation()

    const {
        scheduledTasksTypes,
        filteredScheduledTasksTypes,
        scheduledTasksSearchText,
        loading,
        error
    } = useAppSelector(store => store.scheduledTasks)

    useEffect(() => {
        if (instanceId) {
            dispatch(getScheduledTasksThunk(instanceId))
        }
    }, [])

    if (loading) {
        return <Loader />
    }

    if (error) {
        return error
    }

    const scheduledTasks = filteredScheduledTasksTypes.length ? filteredScheduledTasksTypes : scheduledTasksTypes
    const noDataAfterSearch = !!scheduledTasksSearchText && !filteredScheduledTasksTypes.length;

    return (
        <>
            <PageSearch onChange={value => dispatch(filterScheduledTasks(value))} />

            <EmptyHandler isEmpty={noDataAfterSearch}>
                {scheduledTasks.map(({ type, tasks }) => {
                    const isCron = type === 'cron'
                    const isCustom = type === 'custom'

                    if (isCustom) {
                        return null
                    }

                    return (
                        <div key={type} className={styles.SectionWrapper}>
                            <div className={`MediumTitle ${styles.TaskType}`}>
                                {getScheduledTasksTypes(t)[type as TaskKey]}
                            </div>

                            <div className='CustomizedAntdTable'>
                                <TableHeader isCron={isCron} />

                                <EmptyHandler isEmpty={!tasks.length}>
                                    {tasks.map((task: ICron | IFixedTasks, index: number) => (
                                        <TableRow task={task} isCron={isCron} key={index} />
                                    ))}
                                </EmptyHandler>
                            </div>
                        </div>
                    )
                })}
            </EmptyHandler>
        </>
    )
};

export default ScheduledTasks