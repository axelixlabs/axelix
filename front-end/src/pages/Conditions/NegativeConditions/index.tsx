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
import { useVirtualizer } from "@tanstack/react-virtual";

import { useRef } from "react";

import { Copy } from "components";
import { EConditionStatus, type IConditionBeanNegative } from "models";

import { ConditionsAccordionEntry } from "../ConditionAccordionEntry";
import styles from "../styles.module.css";

interface IProps {
    /**
     * Negative or positive match
     */
    negativeMatches: IConditionBeanNegative[];
}

export const NegativeConditions = ({ negativeMatches }: IProps) => {
    const ref = useRef<HTMLDivElement>(null);

    const rowVirtualizer = useVirtualizer({
        count: negativeMatches.length,
        getScrollElement: () => ref.current,
        estimateSize: () => 103,
        scrollPaddingStart: 80,
        gap: 40,
    });

    const virtualItems = rowVirtualizer.getVirtualItems();

    return (
        <>
            <div ref={ref} className={styles.ConditionsMainWrapper}>
                <div
                    style={{
                        height: `${rowVirtualizer.getTotalSize()}px`,
                    }}
                    className={styles.ConditionsInnerWrapper}
                >
                    {virtualItems.map(({ key, index, start }) => {
                        const { className, methodName, matched, notMatched } = negativeMatches[index];

                        const itemsWithStatus = [
                            ...notMatched.map((item) => ({
                                ...item,
                                status: EConditionStatus.NOT_MATCHED,
                            })),
                            ...matched.map((item) => ({
                                ...item,
                                status: EConditionStatus.MATCHED,
                            })),
                        ];

                        return (
                            <div
                                key={key}
                                data-index={index}
                                ref={rowVirtualizer.measureElement}
                                className={styles.VirtualItem}
                                style={{
                                    transform: `translateY(${start}px)`,
                                }}
                            >
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
                                                <Copy text={methodName} />
                                            </div>
                                        </>
                                    )}
                                </div>
                                <ConditionsAccordionEntry items={itemsWithStatus} />
                            </div>
                        );
                    })}
                </div>
            </div>
        </>
    );
};
