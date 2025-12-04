import { TooltipWithCopy } from "components";
import type { IFixedTasks } from "models";

import { ScheduledTasksStatusSwitch } from "../../ScheduledTasksStatusSwitch";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Task body for scheduled task types
     */
    task: IFixedTasks;
}

export const FixedTaskTableRow = ({ task }: IProps) => {
    return (
        <div className={`TableRow ${styles.FixedTaskTableRow}`}>
            <div className={`RowChunk ${styles.TooltipWrapperChunk}`}>
                <TooltipWithCopy text={task.runnable.target} />
            </div>
            <div className="RowChunk">{task.initialDelay}</div>
            <div className="RowChunk">{task.interval}</div>
            <div className="RowChunk">
                <ScheduledTasksStatusSwitch runnable={task} />
            </div>
        </div>
    );
};
