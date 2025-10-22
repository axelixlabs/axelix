import { TooltipWithCopy } from "components";
import type { IFixedTasks } from "models";

import { OnOffSwitch } from "../OnOffSwitch";

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
                <OnOffSwitch runnable={task} />
            </div>
        </div>
    );
};
