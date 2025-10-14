import { useTranslation } from 'react-i18next';

import styles from './styles.module.css'

interface IProps {
    
    /**
     * If true, this is a table for Cron task
     */
    isCron: boolean
}

// TODO:
//  I do not think that this is even supposed to be a single component, to be honest.
//  We have a lot of conditional logic inside depending on the cron task flag. Ideally,
//  we would want to separate fixedDelay / fixedRate components from the cron,
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
                  {
                    // TODO: that also feels wrong to be honest. This br inside the initialDelay is
                    //  actually a hack, we need to find a way around this
                  }
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