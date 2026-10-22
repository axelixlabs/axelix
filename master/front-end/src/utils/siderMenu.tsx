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
import type { TFunction } from "i18next";
import { Link } from "react-router";

import {
    BeansIcon,
    CachesIcon,
    ConditionsIcon,
    ConfigPropsIcon,
    DependenciesIcon,
    DetailsIcon,
    EntitiesMapIcon,
    EnvironmentIcon,
    GCIcon,
    InsightsIcon,
    JavaMonoIcon,
    LoggersIcon,
    MetricsIcon,
    ScheduledTasksIcon,
    SpringIcon,
    ThreadDumpIcon,
    TransactionIcon,
} from "@/assets";
import { UnavailableFeatureTooltip } from "@/components";
import { EInstanceFeature, type ISiderMenuItem, type TMenuItem } from "@/models";

const createMenuItems = (items: ISiderMenuItem[], availableFeatures: EInstanceFeature[] | null): TMenuItem[] => {
    return items.map(({ path, icon, label, feature }) => {
        // Unknown features (the profile is not loaded yet, or failed to load) never grey anything out
        if (feature && availableFeatures && !availableFeatures.includes(feature)) {
            return {
                key: path,
                icon: icon,
                disabled: true,
                label: <UnavailableFeatureTooltip feature={feature}>{label}</UnavailableFeatureTooltip>,
            };
        }

        return {
            key: path,
            icon: icon,
            label: <Link to={path}>{label}</Link>,
        };
    });
};

/**
 * @param availableFeatures the features the instance has, or {@code null} while they are unknown.
 */
export const getInstanceItems = (
    instanceId: string,
    t: TFunction,
    availableFeatures: EInstanceFeature[] | null,
): TMenuItem[] => {
    const basePath = `/instance/${instanceId}`;

    const generalMenuItemsData: ISiderMenuItem[] = [
        {
            path: `${basePath}/details`,
            icon: <DetailsIcon />,
            label: t("Sider.overview"),
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
        {
            path: `${basePath}/dependencies`,
            icon: <DependenciesIcon />,
            label: t("Sider.dependenciesAnalyzer"),
            feature: EInstanceFeature.DEPENDENCY_ANALYSIS,
        },
    ];

    const springMenuItemsData: ISiderMenuItem[] = [
        {
            path: `${basePath}/environment`,
            icon: <EnvironmentIcon />,
            label: t("Sider.environment"),
        },
        {
            path: `${basePath}/config-props`,
            icon: <ConfigPropsIcon />,
            label: t("Sider.configurationProperties"),
        },
        {
            path: `${basePath}/beans`,
            icon: <BeansIcon />,
            label: t("Sider.beans"),
        },
        {
            path: `${basePath}/conditions`,
            icon: <ConditionsIcon />,
            label: t("Sider.conditions"),
        },
        {
            path: `${basePath}/scheduled-tasks`,
            icon: <ScheduledTasksIcon />,
            label: t("Sider.scheduledTasks"),
            feature: EInstanceFeature.SCHEDULED_TASKS,
        },
        {
            path: `${basePath}/caches`,
            icon: <CachesIcon />,
            label: t("Sider.caches"),
            feature: EInstanceFeature.CACHES,
        },
        {
            path: `${basePath}/transactional`,
            icon: <TransactionIcon />,
            label: t("Sider.transactionControl"),
            feature: EInstanceFeature.TRANSACTIONS,
        },
    ];

    const jpaMenuItemsData: ISiderMenuItem[] = [
        {
            path: `${basePath}/entities-map`,
            icon: <EntitiesMapIcon />,
            label: t("Sider.entitiesMap"),
            feature: EInstanceFeature.ENTITIES_MAP,
        },
    ];

    const jvmMenuItemsData: ISiderMenuItem[] = [
        {
            path: `${basePath}/thread-dump`,
            icon: <ThreadDumpIcon />,
            label: t("Sider.threadDump"),
        },
        {
            path: `${basePath}/garbage-collector`,
            icon: <GCIcon />,
            label: t("Sider.garbageCollector"),
        },
    ];

    return [
        {
            key: "general",
            icon: <InsightsIcon />,
            label: t("Sider.general"),
            children: createMenuItems(generalMenuItemsData, availableFeatures),
        },
        {
            key: "JVM",
            icon: <JavaMonoIcon />,
            label: "Java",
            children: createMenuItems(jvmMenuItemsData, availableFeatures),
        },
        {
            key: "spring",
            icon: <SpringIcon />,
            label: "Spring Boot",
            children: createMenuItems(springMenuItemsData, availableFeatures),
        },
        {
            key: "jpa",
            icon: <EntitiesMapIcon />,
            label: "JPA",
            children: createMenuItems(jpaMenuItemsData, availableFeatures),
        },
    ];
};
