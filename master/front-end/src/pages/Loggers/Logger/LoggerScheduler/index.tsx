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
import { useState } from "react";
import { useTranslation } from "react-i18next";

import type { TChangeLoggerLevel } from "models";

import { LoggerTimePicker } from "./LoggerTimePicker";
import styles from "./styles.module.css";

import { TimerIcon } from "assets";

export interface IProps {
    /**
     * Callback to change the logger level.
     */
    handleChange: TChangeLoggerLevel;

    /**
     * All available logging levels.
     */
    levels: string[];

    /**
     * Currently active (effective) level.
     */
    checkedLevel: string;

    /**
     * Formatted remaining time string (e.g. "2h 5m"), or null if no temporary level.
     */
    remainingTime: string | null;
}

export const LoggerScheduler = ({ handleChange, levels, checkedLevel, remainingTime }: IProps) => {
    const { t } = useTranslation();

    const [popoverOpen, setPopoverOpen] = useState<boolean>(false);
    const [selectedLevel, setSelectedLevel] = useState<string>(checkedLevel);

    return (
        <Popover
            title={
                <div className={styles.PopoverHeaderWrapper}>
                    {t("Loggers.setTemporaryLevel")}

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
            destroyOnHidden
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
                <span className={styles.Badge}>
                    <span className={styles.BadgeDot} />
                    <span className={`TextUltraSmall ${styles.BadgeContent}`}>{remainingTime}</span>
                </span>
            ) : (
                <TimerIcon className={styles.TimerIcon} />
            )}
        </Popover>
    );
};
