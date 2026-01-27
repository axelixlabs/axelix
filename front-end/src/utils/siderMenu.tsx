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
import { Link } from "react-router-dom";

import type { MenuItem as AntdMenuItem, IMenuItem } from "models";

const createMenuItems = (items: IMenuItem[]): AntdMenuItem[] => {
    return items.map(({ path, icon, label }) => ({
        key: path,
        icon: icon,
        label: <Link to={path}>{label}</Link>,
    }));
};

export const getItems = (instanceId: string, t: TFunction): AntdMenuItem[] => {
    const basePath = `/instance/${instanceId}`;

    const insightsMenuItemsData: IMenuItem[] = [
        {
            path: `${basePath}/details`,
            icon: <DetailsIcon />,
            label: t("Sider.details"),
        },
        {
            path: `${basePath}/metrics`,
            icon: <MetricsIcon />,
            label: t("Sider.metrics"),
        },
        {
            path: `${basePath}/loggers`,
            icon: <LoggersIcon />,
            label: t("Sider.loggers"),
        },
    ];

    const springMenuItemsData: IMenuItem[] = [
        {
            path: `${basePath}/environment`,
            icon: <EnvironmentIcon />,
            label: t("Sider.environment"),
        },
        {
            path: `${basePath}/beans`,
            icon: <BeansIcon />,
            label: t("Sider.beans"),
        },
        {
            path: `${basePath}/config-props`,
            icon: <ConfigPropsIcon />,
            label: t("Sider.configurationProperties"),
        },
        {
            path: `${basePath}/scheduled-tasks`,
            icon: <ScheduledTasksIcon />,
            label: t("Sider.scheduledTasks"),
        },
        {
            path: `${basePath}/conditions`,
            icon: <ConditionsIcon />,
            label: t("Sider.conditions"),
        },
        {
            path: `${basePath}/caches`,
            icon: <CachesIcon />,
            label: t("Sider.caches"),
        },
        {
            path: `${basePath}/transactional`,
            icon: <TransactionIcon />,
            label: t("Sider.transactionControl"),
        },
    ];

    const jvmMenuItemsData: IMenuItem[] = [
        {
            path: `${basePath}/thread-dump`,
            icon: <ThreadDumpIcon />,
            label: t("Sider.threadDump"),
        },
        {
            path: `${basePath}/garbage-collector`,
            icon: <GarbageCollectorIcon />,
            label: t("Sider.garbageCollector"),
        },
    ];

    return [
        {
            key: "insights",
            icon: <InsightsIcon />,
            label: t("Sider.insights"),
            children: createMenuItems(insightsMenuItemsData),
        },
        {
            key: "spring",
            icon: <SpringIcon />,
            label: "Spring Framework",
            children: createMenuItems(springMenuItemsData),
        },
        {
            key: "JVM",
            icon: <JvmIcon />,
            label: "JVM",
            children: createMenuItems(jvmMenuItemsData),
        },
    ];
};
