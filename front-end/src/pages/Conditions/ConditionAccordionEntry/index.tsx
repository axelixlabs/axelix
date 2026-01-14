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
import CheckmarkIcon from "assets/icons/checkmark.svg?react";
import CloseIcon from "assets/icons/close.svg?react";

import { Accordion } from "components";
import { EConditionStatus, type ICondition } from "models";

import styles from "./styles.module.css";

interface IStatusAwareCondition extends ICondition {
    status: EConditionStatus;
}

interface IProps {
    items: IStatusAwareCondition[];
}

export const ConditionsAccordionEntry = ({ items }: IProps) => {
    const findNeededIcon = (status: EConditionStatus) => {
        if (status === EConditionStatus.NOT_MATCHED) {
            return <CloseIcon />;
        }

        return <CheckmarkIcon />;
    };

    return (
        <div className={`AccordionsWrapper ${styles.AccordionsWrapper}`}>
            {items.map(({ message, condition, status }) => (
                <Accordion
                    header={
                        <div className={styles.LabelWrapper}>
                            {findNeededIcon(status)}
                            {condition}
                        </div>
                    }
                    key={`${message} ${condition}`}
                >
                    <div className={styles.Message}>{message}</div>
                </Accordion>
            ))}
        </div>
    );
};
