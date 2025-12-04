import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";

export const FixedTaskTableHeader = () => {
    const { t } = useTranslation();

    return (
        <div className={`TableHeader ${styles.TableHeader}`}>
            <div className="RowChunk">{t("ScheduledTasks.runnable")}</div>
            <div dangerouslySetInnerHTML={{ __html: t("ScheduledTasks.initialDelay") }} className="RowChunk" />
            <div className="RowChunk">{t("ScheduledTasks.interval")}</div>
            <div className={`RowChunk ${styles.Status}`}>{t("status")}</div>
        </div>
    );
};
