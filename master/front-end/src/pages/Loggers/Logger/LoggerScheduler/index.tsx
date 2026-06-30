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

import { LoggerTimePicker } from "./LoggerTimePicker";
import styles from "./styles.module.css";

import { TimerIcon } from "assets";

export interface IProps {
    checkedLevel: string;
    handleChange: (level: string, timerSeconds: number) => void;
    levels: string[];
}

export const LoggerScheduler = ({ checkedLevel, handleChange, levels }: IProps) => {
    const [popoverOpen, setPopoverOpen] = useState<boolean>(false);
    const [selectedLevel, setSelectedLevel] = useState<string>(checkedLevel);

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
        >
            <TimerIcon className={styles.TimerIcon} />
        </Popover>
    );
};
