import { TooltipWithCopy } from "components";
import { type ICron } from "models";

import { ScheduledTasksStatusSwitch } from "../../ScheduledTasksStatusSwitch";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Cron task to render
     */
    task: ICron;
}

export const CronTaskTableRow = ({ task }: IProps) => {
    return (
        <div className={`TableRow ${styles.CronTaskTableRow}`}>
            <div className={`RowChunk ${styles.TooltipWrapperChunk}`}>
                <TooltipWithCopy text={task.runnable.target} />
            </div>
            <div className="RowChunk">{task.expression}</div>
            <div className="RowChunk">
                <ScheduledTasksStatusSwitch runnable={task} />
            </div>
        </div>
    );
};
