import type { TFunction } from "i18next";

export const getScheduledTasksTypes = (t: TFunction) => ({
    cron: t("ScheduledTasks.cron"),
    fixedDelay: t("ScheduledTasks.fixedDelay"),
    fixedRate: t("ScheduledTasks.fixedRate")
})