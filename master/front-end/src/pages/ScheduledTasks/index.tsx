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
import { useParams } from "react-router";

import { EmptyHandler, Loader } from "@/components";
import { fetchData, filterScheduledTasks, getScheduledTaskItems } from "@/helpers";
import { type IScheduledTasksResponseBody, StatefulRequest } from "@/models";
import { getScheduledTasksData } from "@/services";

import { TaskDetail } from "./TaskDetail";
import { TaskList } from "./TaskList";
import styles from "./styles.module.css";

const ScheduledTasks = () => {
    const { instanceId } = useParams();

    const [scheduledTasks, setScheduledTasks] = useState(StatefulRequest.loading<IScheduledTasksResponseBody>());
    const [search, setSearch] = useState("");
    const [selectedTaskId, setSelectedTaskId] = useState<string>();

    const loadScheduledTasks = (): Promise<void> =>
        fetchData(setScheduledTasks, () => getScheduledTasksData(instanceId!));

    useEffect(() => {
        loadScheduledTasks();
    }, []);

    if (scheduledTasks.loading) {
        return <Loader />;
    }

    if (scheduledTasks.error) {
        return <EmptyHandler isEmpty />;
    }

    const scheduledTasksData = scheduledTasks.response!;
    const items = getScheduledTaskItems(scheduledTasksData);
    const selectedItem = items.find(({ task }) => task.taskId === selectedTaskId) ?? items[0];

    const effectiveScheduledTasks = search ? filterScheduledTasks(scheduledTasksData, search) : scheduledTasksData;

    return (
        <div className={styles.Layout}>
            <TaskList
                scheduledTasks={effectiveScheduledTasks}
                selectedTaskId={selectedItem?.task.taskId}
                onSelect={setSelectedTaskId}
                setSearch={setSearch}
            />

            {selectedItem && (
                <TaskDetail key={selectedItem.task.taskId} item={selectedItem} onChange={loadScheduledTasks} />
            )}
        </div>
    );
};

export default ScheduledTasks;
