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
import { useTranslation } from "react-i18next";

import { loggersColors } from "utils";

import styles from "./styles.module.css";

import { TargetIcon } from "assets";

interface IProps {
    /**
     * All possible logging levels that are supported by the logging system inside the instance
     */
    levels: string[];

    /**
     * The logging level inside the {@link levels} array, that is considered to be currently active.
     */
    checkedLevel?: string;

    /**
     * The configured level of a logger group, if any
     */
    configuredLevel?: string;

    /**
     * The function that should update the level
     */
    handleChange: (level: string) => void;
}

export const Levels = ({ levels, checkedLevel, configuredLevel, handleChange }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.MainWrapper}>
                {levels.map((level) => {
                    const color = loggersColors[level] || loggersColors.DEFAULT;

                    return (
                        <div className={styles.RadioGroupWrapper} key={level}>
                            <label
                                className={`${styles.RadioButton} ${checkedLevel === level ? styles.Selected : ""}`}
                                style={
                                    {
                                        "--color-primary": color.colorPrimary,
                                        "--color-primary-hover": color.colorPrimaryHover,
                                        "--color-primary-active": color.colorPrimaryActive,
                                    } as React.CSSProperties
                                }
                            >
                                <input
                                    type="radio"
                                    value={level}
                                    checked={checkedLevel === level}
                                    onChange={() => handleChange(level)}
                                />
                                {level}
                            </label>
                            {configuredLevel === level && (
                                <Tooltip title={t("Loggers.configuredExplicitly")} className={styles.Tooltip}>
                                    <TargetIcon className={styles.TargetIcon} />
                                </Tooltip>
                            )}
                        </div>
                    );
                })}
            </div>
        </>
    );
};
