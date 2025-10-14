import { useTranslation } from 'react-i18next';

import styles from './styles.module.css'

interface IProps {
    /**
     * If true, this is a table for Cron task
     */
    isCron: boolean
}

export const TableHeader = ({ isCron }: IProps) => {
    const { t } = useTranslation()

    return (
        <div className={`TableHeader ${isCron ? styles.CronTableHeader : styles.TableHeader}`}>
            <div className="RowChunk">
                {t("ScheduledTasks.runnable")}
            </div>

            {isCron ? (
                <div className="RowChunk">
                    {t("ScheduledTasks.expression")}
                </div>
            ) : (
                <>
                    <div
                        dangerouslySetInnerHTML={{ __html: t("ScheduledTasks.initialDelay") }}
                        className="RowChunk"
                    />
                    <div className="RowChunk">
                        {t("ScheduledTasks.interval")}
                    </div>
                </>
            )}

            <div className={`RowChunk ${styles.Status}`}>
                {t("ScheduledTasks.status")}
            </div>
        </div>
    )
};