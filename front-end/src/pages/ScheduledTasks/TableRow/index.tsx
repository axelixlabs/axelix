import { Switch } from "antd";
import { useTranslation } from "react-i18next";

import type { ICron, IFixedTasks } from "models";
import { TooltipWithCopy } from "components";

import styles from './styles.module.css'

interface IProps {
    /**
     * Task body for scheduled task types
     */
    task: ICron | IFixedTasks;
    /**
     * If true, this is a table for Cron task
     */
    isCron: boolean;
}

// TODO: Same thing as with table header - split into two components.
export const TableRow = ({ task, isCron }: IProps) => {
    const { t } = useTranslation()

    return (
        <div className={`TableRow ${isCron ? styles.CronTableRow : styles.TableRow}`}>
            <div className={`RowChunk ${styles.TooltipWrapperChunk}`}>
                <TooltipWithCopy text={task.runnable.target} />
            </div>

            {isCron ? (
                <div className="RowChunk">{(task as ICron).expression}</div>
            ) : (
                <>
                    <div className="RowChunk">{(task as IFixedTasks).initialDelay}</div>
                    <div className="RowChunk">{(task as IFixedTasks).interval}</div>
                </>
            )}

            <div className="RowChunk">
                <Switch
                    checkedChildren={t("ScheduledTasks.on")}
                    unCheckedChildren={t("ScheduledTasks.off")}
                    checked
                />
            </div>
        </div>
    )
};