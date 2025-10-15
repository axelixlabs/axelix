import styles from "./styles.module.css";
import {TooltipWithCopy} from "components";
import {type ICron} from "models";
import {OnOffSwitch} from "../OnOffSwitch";

interface IProps {

  /**
   * Cron task to render
   */
  task: ICron;
}

export const CronTaskTableRow = ({ task } : IProps) => {
  return (
    <>
      <div className={`TableRow ${styles.CronTaskTableRow}`}>
        <div className={`RowChunk ${styles.TooltipWrapperChunk}`}>
          <TooltipWithCopy text={task.runnable.target} />
        </div>
        <div className="RowChunk">{task.expression}</div>
        <div className="RowChunk">
          <OnOffSwitch runnable={task}/>
        </div>
      </div>
    </>
  )
}
