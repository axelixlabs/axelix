import type { MenuItem } from "models";
import type { TFunction } from "i18next";

export const getItems = (instanceId: string, t: TFunction): MenuItem[] => {
    return ([
        {
            key: "insights",
            label: t("insights"),
            children: [
                { key: `/instance/${instanceId}/details`, label: t("details") },
                { key: `/instance/${instanceId}/metrics`, label: t("metrics") },
                { key: `/instance/${instanceId}/environment`, label: t("environment") },
                { key: `/instance/${instanceId}/beans`, label: "Beans" },
                { key: `/instance/${instanceId}/config-props`, label: t("configurationProperties") },
                { key: `/instance/${instanceId}/scheduled-tasks`, label: t("scheduledTasks") },
            ],
        },
        { key: `/instance/${instanceId}/loggers`, label: t("loggers") },
        { key: `/instance/${instanceId}/jvm`, label: "JVM" },
        { key: `/instance/${instanceId}/mappings`, label: t("mappings") },
        { key: `/instance/${instanceId}/caches`, label: t("caches") },
    ])
}