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
import { Input } from "antd";
import type { Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import type { IScheduledTasksTaskGroup } from "@/models";
import { SCHEDULED_TASKS_GROUP_LABEL_KEY } from "@/utils";

import { ScheduledTasksTaskListRow } from "./ScheduledTasksTaskListRow";
import styles from "./styles.module.css";

interface IProps {
    groups: IScheduledTasksTaskGroup[];
    selectedKey: string;
    onSelect: (key: string) => void;
    search: string;
    onSearch: Dispatch<SetStateAction<string>>;
}

export const TaskList = ({ groups, selectedKey, onSelect, search, onSearch }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.MainWrapper}>
                <Input
                    allowClear
                    value={search}
                    placeholder={t("ScheduledTasks.searchTasks")}
                    onChange={(event) => onSearch(event.target.value)}
                />

                <div className={styles.GroupsWrapper}>
                    {groups.map(({ type, tasks }) => {
                        return (
                            <div className={styles.Group} key={type}>
                                <div className={`TextUltraSmall ${styles.GroupTitle}`}>
                                    {t(SCHEDULED_TASKS_GROUP_LABEL_KEY[type])} · {tasks.length}
                                </div>

                                {tasks.map((task) => {
                                    return (
                                        <ScheduledTasksTaskListRow
                                            task={task}
                                            selectedKey={selectedKey}
                                            onSelect={onSelect}
                                        />
                                    );
                                })}
                            </div>
                        );
                    })}
                </div>
            </div>
        </>
    );
};
