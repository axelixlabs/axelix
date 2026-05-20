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

import { useEffect, useRef } from "react";
import { useLocation } from "react-router";

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
    const ref = useRef<HTMLDivElement>(null);
    const { hash } = useLocation();

    const rowVirtualizer = useVirtualizer({
        count: positiveMatches.length,
        getScrollElement: () => ref.current,
        estimateSize: () => 103,
        scrollPaddingStart: 80,
        gap: 40,
    });

    const targetIndex = positiveMatches.findIndex(({ className, methodName }) => {
        const full = `${className}${methodName ?? ""}`;
        return normalizeHtmlElementId(full) === normalizeHtmlElementId(hash);
    });

    useEffect(() => {
        if (!hash || targetIndex === -1) {
            return;
        }

        rowVirtualizer.scrollToIndex(targetIndex, {
            align: "start",
            behavior: "smooth",
        });
    }, []);

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
                        const { className, methodName, matched } = positiveMatches[index];

                        const items = matched.map((item) => {
                            return {
                                ...item,
                                status: EConditionStatus.MATCHED,
                            };
                        });

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
                                <div
                                    className={`${styles.ConditionHeaderWrapper} ${index === targetIndex ? "Highlight" : ""}`}
                                >
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
                                <ConditionsAccordionEntry items={items} />
                            </div>
                        );
                    })}
                </div>
            </div>
        </>
    );
};
