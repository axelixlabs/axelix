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
        { key: `/instance/${instanceId}/mappings`, label: t("Sider.mappings") },
        { key: `/instance/${instanceId}/caches`, label: t("Sider.caches") },
    ];
};
