import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";

export const CronTableHeader = () => {
    const { t } = useTranslation();

    return (
        <>
            <div className={`TableHeader ${styles.CronTableHeader}`}>
                <div className="RowChunk">{t("ScheduledTasks.runnable")}</div>
                <div className="RowChunk">{t("ScheduledTasks.expression")}</div>
                <div className={`RowChunk ${styles.Status}`}>{t("status")}</div>
            </div>
        </>
    );
};
