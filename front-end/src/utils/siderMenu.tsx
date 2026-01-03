/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import SpringIcon from "assets/icons/spring_black.svg?react";
import ThreadDumpIcon from "assets/icons/threadDump.svg?react";
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
