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
import { Popover, Select } from "antd";
import dayjs from "dayjs";
import { useEffect, useState } from "react";

import type { ILogger } from "models";

import { LoggerTimePicker } from "./LoggerTimePicker";
import styles from "./styles.module.css";

import { TimerIcon } from "assets";

export interface IProps {
    handleChange: (level: string, timerSeconds: number) => void;
    levels: string[];
    logger: ILogger;
}

export const LoggerScheduler = ({ handleChange, levels, logger }: IProps) => {
    const { effectiveLevel, temporaryLevelInitiatedAt, temporaryLevelRollsBackAt } = logger;

    const [popoverOpen, setPopoverOpen] = useState<boolean>(false);
    const [selectedLevel, setSelectedLevel] = useState<string>(effectiveLevel);
    const [remainingTime, setRemainingTime] = useState<string | null>(null);

    useEffect(() => {
        if (!temporaryLevelInitiatedAt || !temporaryLevelRollsBackAt) {
            setRemainingTime(null);
            return;
        }

        const endTime = dayjs(temporaryLevelRollsBackAt);

        if (!endTime.isValid()) {
            setRemainingTime(null);
            return;
        }

        const updateTimer = () => {
            const diffMs = endTime.diff(dayjs());

            if (diffMs <= 0) {
                setRemainingTime(null);
                return;
            }

            const totalSeconds = Math.floor(diffMs / 1000);
            const hours = Math.floor(totalSeconds / 3600);
            const minutes = Math.floor((totalSeconds % 3600) / 60);

            const formattedHours = String(hours).padStart(2, "0");
            const formattedMinutes = String(minutes).padStart(2, "0");

            const time = `${formattedHours}:${formattedMinutes}`;
            setRemainingTime(time);
        };

        updateTimer();

        const interval = setInterval(updateTimer, 60000);

        return () => clearInterval(interval);
    }, []);

    return (
        <Popover
            destroyOnHidden
            title={
                <div className={styles.PopoverHeaderWrapper}>
                    Placeholder
                    <Select
                        size="small"
                        value={selectedLevel}
                        onChange={setSelectedLevel}
                        options={levels.map((level) => {
                            return {
                                value: level,
                            };
                        })}
                        className={styles.LevelSelect}
                    />
                </div>
            }
            content={
                <LoggerTimePicker
                    handleChange={handleChange}
                    selectedLevel={selectedLevel}
                    setPopoverOpen={setPopoverOpen}
                />
            }
            trigger="click"
            open={popoverOpen}
            onOpenChange={setPopoverOpen}
            styles={{
                content: {
                    overflow: "hidden",
                },
                title: {
                    marginBottom: "20px",
                },
            }}
        >
            {remainingTime ? (
                <span className={styles.TimerText}>{remainingTime}</span>
            ) : (
                <TimerIcon className={styles.TimerIcon} />
            )}
        </Popover>
    );
};
