/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { Tooltip } from "antd";

import type { IMetric } from "models";

import styles from "./styles.module.css";

import InfoIcon from "assets/icons/info.svg";
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

            {/* TODO: Maybe we would want to extract that into a separate component in the future? */}
            {metric.description && (
                <Tooltip
                    title={
                        <div className={styles.TooltipContentWrapper}>
                            <div>
                                <img src={InfoIcon} alt="Info icon" className={styles.InfoIcon} />
                            </div>
                            <div>{metric.description}</div>
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
