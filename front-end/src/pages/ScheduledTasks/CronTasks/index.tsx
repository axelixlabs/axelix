import styles from "../styles.module.css";
import {TableHeader} from "../TableHeader";
import {EmptyHandler} from "components";
import type {ICron} from "models";
import {useTranslation} from "react-i18next";
import {CronTaskTableRow} from "../CronTaskTableRow";

interface IProps {

  /**
   * List of cron tasks to be rendered
   */
  cronTasks: ICron[];
}

export const CronTasks = ({ cronTasks } : IProps) => {

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
                <CronTaskTableRow task={task} key={index} />)
              )
            }
          </EmptyHandler>
        </div>
      </div>
    </>
  );
}