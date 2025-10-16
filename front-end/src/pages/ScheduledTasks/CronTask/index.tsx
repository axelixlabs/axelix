import styles from "../styles.module.css";
import {TableHeader} from "../TableHeader";
import {EmptyHandler} from "components";
import type {ICron} from "models";
import {TableRow} from "../TableRow";
import {useTranslation} from "react-i18next";

interface IProps {

  /**
   * List of cron tasks to be rendered
   */
  cronTasks: ICron[];
}

export const CronTask = ( { cronTasks } : IProps) => {

  const { t } = useTranslation();

  return (
    <>
      <div key="cron" className={styles.SectionWrapper}>
        <div className={`MediumTitle ${styles.TaskType}`}>
          {t("ScheduledTasks.cron")}
        </div>

        <div className='CustomizedAntdTable'>
          <TableHeader isCron={true} />

          <EmptyHandler isEmpty={cronTasks.length === 0}>
            {
              cronTasks.map((task: ICron, index: number) => (
                <TableRow task={task} isCron={true} key={index} />)
              )
            }
          </EmptyHandler>
        </div>
      </div>
    </>
  );
}