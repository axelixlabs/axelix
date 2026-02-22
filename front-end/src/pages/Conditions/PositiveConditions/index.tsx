/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
                                    <div>Class:</div>
                                    <div className={styles.Value}>{className}</div>
                                    <Copy text={className} />
                                </div>
                                {methodName && (
                                    <>
                                        <div className={styles.ConditionHeaderSection}>
                                            <div>Method:</div>
                                            <div className={styles.Value}>{methodName}</div>
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
