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
import { Popover } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { getQueryBarWidth, getQueryLeftPosition, toDateTimeWithMs } from "helpers";
import type { IQueryData } from "models";

import { QueryPreview } from "../QueryPreview";
import { SQLBlock } from "../SQLBlock";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The query executed during a particular transaction.
     */
    query: IQueryData;

    /**
     * UNIX timestamp (milliseconds from epoch) when transaction started
     */
    executionStartTimestampMs: number;

    /**
     * Calculated pixels per millisecond
     */
    pxPerMs: number;

    /**
     * The width of the timeline
     */
    timelineWidth: number;
}

export const QueryBar = ({ query, pxPerMs, executionStartTimestampMs, timelineWidth }: IProps) => {
    const { t } = useTranslation();

    const [selectedQuery, setSelectedQuery] = useState<IQueryData | null>(null);
    const [isClosing, setIsClosing] = useState<boolean>(false);

    const { endTimestampMs, startTimestampMs, sql, queryId } = query;

    const isQuerySelected = selectedQuery?.queryId === queryId;
    const durationMs = endTimestampMs - startTimestampMs;

    const handleBarClick = (query: IQueryData, isQuerySelected: boolean): void => {
        if (isQuerySelected) {
            setSelectedQuery(null);
        } else {
            setSelectedQuery(query);
        }
    };

    const handleOnOpenChange = (open: boolean): void => {
        if (!open) {
            setIsClosing(true);
            setSelectedQuery(null);
            setTimeout(() => setIsClosing(false), 300);
        }
    };

    return (
        <Popover
            trigger="hover"
            content={<QueryPreview query={query} />}
            open={isQuerySelected || isClosing ? false : undefined}
            arrow={false}
            styles={{
                container: {
                    padding: "10px",
                    border: "1px solid rgb(204, 204, 204)",
                    borderRadius: 0,
                    boxShadow: "none",
                },
            }}
        >
            <Popover
                trigger="click"
                title={
                    <div className={styles.TitleWrapper}>
                        <div>{t("Transactional.totalTime", { durationMs })}</div>
                        <div>
                            {t("Transactional.startTime")}: {toDateTimeWithMs(startTimestampMs)}
                        </div>
                        <div>
                            {t("Transactional.endTime")}: {toDateTimeWithMs(endTimestampMs)}
                        </div>
                    </div>
                }
                content={<SQLBlock sql={sql} />}
                open={isQuerySelected}
                onOpenChange={handleOnOpenChange}
                placement="left"
                autoAdjustOverflow
            >
                <div
                    className={`${styles.Bar} ${isQuerySelected ? styles.SelectedBar : ""}`}
                    onClick={() => handleBarClick(query, isQuerySelected)}
                    style={{
                        left: getQueryLeftPosition(startTimestampMs, executionStartTimestampMs, pxPerMs, timelineWidth),
                        width: getQueryBarWidth(durationMs, pxPerMs),
                    }}
                />
            </Popover>
        </Popover>
    );
};
