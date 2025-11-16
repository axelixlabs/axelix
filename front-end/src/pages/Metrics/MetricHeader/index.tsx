import styles from "./styles.module.css";

import QuestionIcon from "assets/icons/question.svg";

interface IProps {
    /**
     * Metric name
     */
    name: string;
}

export const MetricHeader = ({ name }: IProps) => {
    return (
        <div className={styles.MainWrapper}>
            <div>{name}</div>
            {/* TODO: Show tootlip after merging Tooltip PR */}
            <img src={QuestionIcon} alt="Question icon" />
        </div>
    );
};
