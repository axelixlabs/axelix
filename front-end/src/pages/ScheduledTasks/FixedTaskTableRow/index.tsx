import type { IFixedTasks } from "models";
import { TooltipWithCopy } from "components";

import styles from './styles.module.css'
import {OnOffSwitch} from "../OnOffSwitch";

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
              <OnOffSwitch runnable={task}/>
            </div>
        </div>
    )
};