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
import type { TFunction } from "i18next";

import type { MenuItem } from "models";

export const getItems = (instanceId: string, t: TFunction): MenuItem[] => {
    return [
        {
            key: "insights",
            label: t("Sider.insights"),
            children: [
                { key: `/instance/${instanceId}/details`, label: t("Sider.details") },
                { key: `/instance/${instanceId}/metrics`, label: t("Sider.metrics") },
                { key: `/instance/${instanceId}/environment`, label: t("Sider.environment") },
                { key: `/instance/${instanceId}/beans`, label: "Beans" },
                { key: `/instance/${instanceId}/config-props`, label: t("Sider.configurationProperties") },
                { key: `/instance/${instanceId}/scheduled-tasks`, label: t("Sider.scheduledTasks") },
                { key: `/instance/${instanceId}/conditions`, label: t("Sider.conditions") },
            ],
        },
        { key: `/instance/${instanceId}/loggers`, label: t("Sider.loggers") },
        { key: `/instance/${instanceId}/jvm`, label: "JVM" },
        { key: `/instance/${instanceId}/thread-dump`, label: t("Sider.threadDump") },
        { key: `/instance/${instanceId}/mappings`, label: t("Sider.mappings") },
        { key: `/instance/${instanceId}/caches`, label: t("Sider.caches") },
    ];
};
