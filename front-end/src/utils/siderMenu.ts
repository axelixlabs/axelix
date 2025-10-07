import type { MenuItem } from "models";
import type { TFunction } from "i18next";

export const getItems = (id: string | undefined, t: TFunction): MenuItem[] => {
    return ([
        {
            key: "insights",
            label: t("insights"),
            children: [
                { key: `/details/${id}`, label: t("details") },
                { key: `/metrics/${id}`, label: t("metrics") },
                { key: `/environment/${id}`, label: t("environment") },
                { key: `/beans/${id}`, label: "Beans" },
                { key: `/config-props/${id}`, label: t("configurationProperties") },
                { key: `/scheduled-tasks/${id}`, label: t("scheduledTasks") },
            ],
        },
        { key: `/loggers/${id}`, label: t("loggers") },
        { key: `/jvm/${id}`, label: "JVM" },
        { key: `/mappings/${id}`, label: t("mappings") },
        { key: `/caches/${id}`, label: t("caches") },
    ])
}