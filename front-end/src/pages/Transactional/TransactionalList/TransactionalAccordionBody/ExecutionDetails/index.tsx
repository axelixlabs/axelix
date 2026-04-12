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
import { Tooltip } from "antd";
import type { Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import { toDateTimeWithMs } from "helpers";
import type { IExecutionWithDurationMs } from "models";

import { QueriesTimeline } from "./QueriesTimeline";
import styles from "./styles.module.css";

import { WarningIcon } from "assets";

interface IProps {
    /**
     * The selected execution
     */
    selectedExecution: IExecutionWithDurationMs | null;

    /**
     * Setter for selecting an execution
     */
    setSelectedExecution: Dispatch<SetStateAction<IExecutionWithDurationMs | null>>;
}

export const ExecutionDetails = ({ selectedExecution, setSelectedExecution }: IProps) => {
    const { t } = useTranslation();

    const queriesLength = selectedExecution?.queries.length;

    return (
        <>
            <div className={`${styles.MainWrapper} ${selectedExecution ? styles.MainWrapperOpened : ""}`}>
                {selectedExecution && (
                    <div className={styles.InnerWrapper}>
                        <hr className={styles.Divider} />
                        <div className={styles.Header}>
                            {t("Transactional.executionDetails")}
                            <div onClick={() => setSelectedExecution(null)} className={styles.Close}>
                                ✖
                            </div>
                        </div>
                        <div className={styles.ExecutionDataWrapper}>
                            <div className={styles.ExecutionDataInnerWrapper}>
                                <div className={styles.ExecutionDataChunk}>
                                    <div>{t("Transactional.queriesCount")}:</div>
                                    <div>{queriesLength}</div>

                                    {queriesLength === 0 && (
                                        <Tooltip
                                            title={
                                                <div className={styles.NoQueriesTooltipTitle}>
                                                    {t("Transactional.noQueries")}
                                                </div>
                                            }
                                            color="#faad14"
                                        >
                                            <WarningIcon className={styles.InfoIcon} />
                                        </Tooltip>
                                    )}
                                </div>
                                <div className={styles.ExecutionDataChunk}>
                                    <div>{t("Transactional.startTime")}:</div>
                                    <div>{toDateTimeWithMs(selectedExecution.startTimestampMs)}</div>
                                </div>
                                <div className={styles.ExecutionDataChunk}>
                                    <div>{t("Transactional.endTime")}:</div>
                                    <div>{toDateTimeWithMs(selectedExecution.endTimestampMs)}</div>
                                </div>
                            </div>
                            <QueriesTimeline selectedExecution={selectedExecution} />
                        </div>
                    </div>
                )}
            </div>
        </>
    );
};
