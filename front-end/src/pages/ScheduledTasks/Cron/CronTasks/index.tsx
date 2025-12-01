import { useTranslation } from "react-i18next";

import { EmptyHandler } from "components";
import type { ICron } from "models";

import styles from "../../styles.module.css";
import { CronTableHeader } from "../CronTableHeader";
import { CronTaskTableRow } from "../CronTaskTableRow";

interface IProps {
    /**
     * List of cron tasks to be rendered
     */
    cronTasks: ICron[];
}

export const CronTasks = ({ cronTasks }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div key="cron" className={styles.SectionWrapper}>
                <div className={`TextMedium ${styles.TaskType}`}>{t("ScheduledTasks.cron")}</div>

                <div className="CustomizedAntdTable">
                    <CronTableHeader />
                    <EmptyHandler isEmpty={cronTasks.length === 0}>
                        {cronTasks.map((task: ICron, index: number) => (
                            <CronTaskTableRow task={task} key={index} />
                        ))}
                    </EmptyHandler>
                </div>
            </div>
        </>
    );
};
