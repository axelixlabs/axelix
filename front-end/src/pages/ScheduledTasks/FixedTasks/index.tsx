import { EmptyHandler } from "components";
import type { IFixedTasks } from "models";

import { FixedTaskTableRow } from "../FixedTaskTableRow";
import { TableHeader } from "../TableHeader";
import styles from "../styles.module.css";

interface IProps {
    /**
     * The title that represents the task type to be displayed.
     * It is expected to be already i18n translated.
     */
    taskTitle: string;

    /**
     * The list of tasks that have a fixed schedule execution timeline, i.e. they
     * are either fixed delay or fixed rate tasks.
     */
    fixedTasks: IFixedTasks[];
}

export const FixedTasks = ({ taskTitle, fixedTasks }: IProps) => {
    return (
        <>
            <div className={styles.SectionWrapper}>
                <div className={`MediumTitle ${styles.TaskType}`}>{taskTitle}</div>

                <div className="CustomizedAntdTable">
                    <TableHeader isCron={false} />

                    <EmptyHandler isEmpty={fixedTasks.length === 0}>
                        {fixedTasks.map((task: IFixedTasks, index: number) => (
                            <FixedTaskTableRow task={task} key={index} />
                        ))}
                    </EmptyHandler>
                </div>
            </div>
        </>
    );
};
