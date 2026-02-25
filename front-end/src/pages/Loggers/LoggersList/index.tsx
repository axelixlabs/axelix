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

import { type Dispatch, type MouseEvent, type SetStateAction, useRef } from "react";

import type { ILogger, StatelessRequest } from "models";

import { Logger } from "../Logger";

import styles from "./styles.module.css";

interface IProps {
    /**
     * All possible logging levels that are supported by the logging system inside the instance
     */
    levels: string[];

    /**
     * The list of loggers
     */
    effectiveLoggers: ILogger[];

    /**
     * setState to update the logger level
     */
    setUpdateLoggerLevel: Dispatch<SetStateAction<StatelessRequest>>;

    /**
     * The function to handle the reset of this given logger
     */
    handleReset: (_: MouseEvent, loggerName: string) => void;
}

export const LoggersList = ({ effectiveLoggers, levels, setUpdateLoggerLevel, handleReset }: IProps) => {
    const parentRef = useRef<HTMLDivElement>(null);

    const rowVirtualizer = useVirtualizer({
        count: effectiveLoggers.length,
        getScrollElement: () => parentRef.current,
        estimateSize: () => 56,
    });

    return (
        <>
            <div ref={parentRef} className={styles.MainWrapper}>
                <div
                    style={{
                        height: `${rowVirtualizer.getTotalSize()}px`,
                    }}
                    className={styles.InnerWrapper}
                >
                    {rowVirtualizer.getVirtualItems().map((virtualItem) => {
                        const { key, size, index, start } = virtualItem;
                        const logger = effectiveLoggers[index];
                        return (
                            <div
                                className={styles.ItemWrapper}
                                style={{
                                    height: `${size}px`,
                                    transform: `translateY(${start}px)`,
                                }}
                                key={key}
                            >
                                <Logger
                                    logger={logger}
                                    levels={levels}
                                    setUpdateLoggerLevel={setUpdateLoggerLevel}
                                    handleReset={handleReset}
                                />
                            </div>
                        );
                    })}
                </div>
            </div>
        </>
    );
};
