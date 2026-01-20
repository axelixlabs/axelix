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
import BeansIcon from "assets/icons/beans.svg?react";
import CachesIcon from "assets/icons/caches.svg?react";
import ConditionsIcon from "assets/icons/conditions.svg?react";
import ConfigPropsIcon from "assets/icons/configProps.svg?react";
import DetailsIcon from "assets/icons/details.svg?react";
import EnvironmentIcon from "assets/icons/environment.svg?react";
import GarbageCollectorIcon from "assets/icons/gc.svg?react";
import InsightsIcon from "assets/icons/insights.svg?react";
import JvmIcon from "assets/icons/jvm.svg?react";
import LoggersIcon from "assets/icons/loggers.svg?react";
import MetricsIcon from "assets/icons/metrics.svg?react";
import ScheduledTasksIcon from "assets/icons/scheduledTasks.svg?react";
import SpringIcon from "assets/icons/spring.svg?react";
import ThreadDumpIcon from "assets/icons/threadDump.svg?react";
import TransactionIcon from "assets/icons/transaction.svg?react";
import type { TFunction } from "i18next";

import type { MenuItem } from "models";

export const getItems = (instanceId: string, t: TFunction): MenuItem[] => {
    return [
        {
            key: "insights",
            icon: <InsightsIcon />,
            label: t("Sider.insights"),
            children: [
                { key: `/instance/${instanceId}/details`, icon: <DetailsIcon />, label: t("Sider.details") },
                { key: `/instance/${instanceId}/metrics`, icon: <MetricsIcon />, label: t("Sider.metrics") },
                { key: `/instance/${instanceId}/loggers`, icon: <LoggersIcon />, label: t("Sider.loggers") },
            ],
        },
        {
            key: "spring",
            icon: <SpringIcon />,
            label: "Spring Framework",
            children: [
                {
                    key: `/instance/${instanceId}/environment`,
                    icon: <EnvironmentIcon />,
                    label: t("Sider.environment"),
                },
                { key: `/instance/${instanceId}/beans`, icon: <BeansIcon />, label: t("Sider.beans") },
                {
                    key: `/instance/${instanceId}/config-props`,
                    icon: <ConfigPropsIcon />,
                    label: t("Sider.configurationProperties"),
                },
                {
                    key: `/instance/${instanceId}/scheduled-tasks`,
                    icon: <ScheduledTasksIcon />,
                    label: t("Sider.scheduledTasks"),
                },
                { key: `/instance/${instanceId}/conditions`, icon: <ConditionsIcon />, label: t("Sider.conditions") },
                { key: `/instance/${instanceId}/caches`, icon: <CachesIcon />, label: t("Sider.caches") },
                { key: `/instance/${instanceId}/transactional`, icon: <TransactionIcon />, label: "@Transactional" },
            ],
        },
        {
            key: "JVM",
            icon: <JvmIcon />,
            label: "JVM",
            children: [
                { key: `/instance/${instanceId}/thread-dump`, icon: <ThreadDumpIcon />, label: t("Sider.threadDump") },
                {
                    key: `/instance/${instanceId}/garbage-collector`,
                    icon: <GarbageCollectorIcon />,
                    label: t("Sider.garbageCollector"),
                },
            ],
        },
    ];
};
