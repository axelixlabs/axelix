import { Tooltip } from "antd";
import { useTranslation } from "react-i18next";

import { statePalette } from "utils";

import styles from "./styles.module.css";

import TargetIcon from "assets/icons/target.svg";

interface IProps {
    /**
     * All possible logging levels that are supported by the logging system inside the instance
     */
    levels: string[];
    /**
     * The entity through which the selected level will be found
     */
    levelCheckEntity?: string;
    /**
     * The configured level of a logger group
     */
    configuredLevel?: string;
    /**
     * The function that should update the level
     */
    handleChange: (level: string) => void;
}

export const Levels = ({ levels, levelCheckEntity, configuredLevel, handleChange }: IProps) => {
    const { t } = useTranslation();

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.LevelsWrapper}>
                {levels.map((level) => {
                    // @ts-expect-error todo fix type in future
                    const color = statePalette[level] || statePalette.DEFAULT;

                    return (
                        <div className={styles.RadioGroupWrapper} key={level}>
                            <label
                                className={`${styles.RadioButton} ${levelCheckEntity === level ? styles.Selected : ""}`}
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
                                    checked={levelCheckEntity === level}
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
