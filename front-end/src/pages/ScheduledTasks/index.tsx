import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

import type { ICron, IFixedTasks, IScheduledTaskItem, TaskKey } from 'models';
import { EmptyHandler, Loader, PageSearch } from "components";
import { useAppDispatch, useAppSelector } from 'hooks';
import { getScheduledTasksThunk } from 'store/thunks';
import { getScheduledTasksTypes } from 'utils';
import { filterScheduledTasks } from 'helpers';
import { TableHeader } from './TableHeader';
import { TableRow } from './TableRow';

import styles from './styles.module.css'

const ScheduledTasks = () => {
    const dispatch = useAppDispatch()
    const { instanceId } = useParams()
    const { t } = useTranslation()

    const { scheduledTasksTypes, loading, error } = useAppSelector(store => store.scheduledTasks)

    const [isSearched, setIsSearched] = useState<boolean>(false)
    const [filteredScheduledTasksTypes, setFilteredScheduledTasksTypes] = useState<IScheduledTaskItem[]>([])

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

    const scheduledTasks = isSearched ? filteredScheduledTasksTypes : scheduledTasksTypes
    const noSearchResults = isSearched && !filteredScheduledTasksTypes.length;

    const handleSearchChange = (search: string): void => {
        const isSearching = Boolean(search);
        setIsSearched(isSearching);

        if (!isSearching) {
            setFilteredScheduledTasksTypes([]);
            return;
        }

        setFilteredScheduledTasksTypes(filterScheduledTasks(scheduledTasksTypes, search));
    };

    return (
        <>
            <PageSearch onChange={handleSearchChange} />

            <EmptyHandler isEmpty={noSearchResults}>
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