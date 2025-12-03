import { Tooltip } from "antd";

import type { IMetric } from "models";

import styles from "./styles.module.css";

import QuestionIcon from "assets/icons/question.svg";

interface IProps {
    /**
     * Single metric
     */
    metric: IMetric;
}

export const MetricHeader = ({ metric }: IProps) => {
    return (
        <div className={styles.MainWrapper}>
            <div>{metric.metricName}</div>

            {/* TODO: Show another Tooltip in future */}
            {metric.description && (
                <Tooltip title={metric.description}>
                    <img src={QuestionIcon} alt="Question icon" />
                </Tooltip>
            )}
        </div>
    );
};
