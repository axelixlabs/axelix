/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { Tooltip } from "antd";
import { useTranslation } from "react-i18next";

import { loggersColors } from "utils";

import styles from "./styles.module.css";

import TargetIcon from "assets/icons/target.svg";

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
        <div className={styles.MainWrapper}>
            <div className={styles.LevelsWrapper}>
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
                                    <img src={TargetIcon} alt="Target icon" className={styles.TargetIcon} />
                                </Tooltip>
                            )}
                        </div>
                    );
                })}
            </div>
            <button className={styles.Reset}>{t("Loggers.reset")}</button>
        </div>
    );
};
