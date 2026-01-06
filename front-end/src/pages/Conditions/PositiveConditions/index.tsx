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
import { Copy } from "components";
import { normalizeHtmlElementId } from "helpers";
import { EConditionStatus, type IConditionBeanPositive } from "models";

import { ConditionsAccordionEntry } from "../ConditionAccordionEntry";
import styles from "../styles.module.css";

interface IProps {
    /**
     * Negative or positive match
     */
    positiveMatches: IConditionBeanPositive[];
}

export const PositiveConditions = ({ positiveMatches }: IProps) => {
    return (
        <>
            {positiveMatches.map(({ className, methodName, matched }) => {
                const items = matched.map((item) => {
                    return {
                        ...item,
                        status: EConditionStatus.MATCHED,
                    };
                });

                const id = normalizeHtmlElementId(`${className}${methodName ? methodName : ""}`);

                return (
                    <>
                        {/* TODO: There is a problem with scrolling here, fix it in the future */}
                        <div id={id} className={styles.ScrollableWrapper} key={id}>
                            <div className={styles.ConditionHeaderWrapper}>
                                <div className={styles.ConditionHeaderSection}>
                                    <span style={{ fontWeight: 300 }}>Class:</span> {className}
                                    <Copy text={className} />
                                </div>
                                {methodName && (
                                    <>
                                        <div className={styles.ConditionHeaderSection}>
                                            <span style={{ fontWeight: 300 }}>Method:</span> {methodName}
                                            <Copy text={className} />
                                        </div>
                                    </>
                                )}
                            </div>
                        </div>
                        <ConditionsAccordionEntry items={items} />
                    </>
                );
            })}
        </>
    );
};
