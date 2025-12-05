import { Tooltip } from "antd";

import type { IMetric } from "models";

import styles from "./styles.module.css";

import QuestionIcon from "assets/icons/question.svg";
import InfoIcon from "assets/icons/info.svg";

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
                <Tooltip
                    title={
                        <div className={styles.TooltipContentWrapper}>
                            <div>
                                <img src={InfoIcon} alt="Info icon" className={styles.InfoIcon} />
                            </div>
                            <div>
                                {metric.description}
                            </div>
                        </div>
                    }
                    placement="right"
                    color="#2196F3"
                >
                    <img src={QuestionIcon} alt="Question icon" />
                </Tooltip>
            )}
        </div>
    );
};
