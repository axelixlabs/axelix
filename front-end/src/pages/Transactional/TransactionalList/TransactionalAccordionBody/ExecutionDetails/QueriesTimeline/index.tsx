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
import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";

import type { ITransactionalExecution } from "models";

import { QueryBar } from "./QueryBar";
import styles from "./styles.module.css";

interface IProps {
    /**
     * The selected execution
     */
    selectedExecution: ITransactionalExecution;
}

export const QueriesTimeline = ({ selectedExecution }: IProps) => {
    const { t } = useTranslation();

    const timelineRef = useRef<HTMLDivElement>(null);
    const [timelineWidth, setTimelineWidth] = useState<number>(0);

    useEffect(() => {
        if (!timelineRef.current) {
            return;
        }

        const observer = new ResizeObserver(([entry]) => {
            setTimelineWidth(entry.contentRect.width);
        });

        observer.observe(timelineRef.current);
        return () => observer.disconnect();
    }, []);

    const executionStartTimestampMs = selectedExecution.startTimestampMs;
    const executionEndTimestampMs = selectedExecution.endTimestampMs;

    const timelineRange = executionEndTimestampMs - executionStartTimestampMs;

    const pxPerMs = timelineWidth / timelineRange;

    return (
        <>
            <div className={styles.MainWrapper}>
                <div>
                    <div className={styles.Timeline} ref={timelineRef}>
                        {selectedExecution.queries.map((query, index) => (
                            <QueryBar
                                key={index}
                                queryNum={index}
                                query={query}
                                pxPerMs={pxPerMs}
                                executionStartTimestampMs={executionStartTimestampMs}
                                timelineWidth={timelineWidth}
                            />
                        ))}
                    </div>
                    <div className={`TextUltraSmall ${styles.Axis}`}>
                        <div>0{t("Transactional.ms")}</div>
                        <div>
                            {timelineRange}
                            {t("Transactional.ms")}
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};
