import { Tooltip } from "antd";
import type { Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { TooltipWithCopy } from "components";
import { type ILogger, StatelessRequest } from "models";
import { setLoggerLevel } from "services";
import { statePalette } from "utils";

import styles from "./styles.module.css";

import TargetIcon from "assets/icons/target.svg";

interface IProps {
    /**
     * All possible logging levels that are supported by the logging system inside the instance
     */
    levels: string[];
    /**
     * Single logger
     */
    logger: ILogger;
    /**
     * setState to update the logger level
     */
    setUpdateLoggerLevel: Dispatch<SetStateAction<StatelessRequest>>;
}

export const Logger = ({ levels, logger, setUpdateLoggerLevel }: IProps) => {
    const { t } = useTranslation();
    const { effectiveLevel, configuredLevel } = logger;
    const { instanceId } = useParams();

    const handleChange = (level: string): void => {
        if (configuredLevel === level) {
            return;
        }

        setUpdateLoggerLevel(StatelessRequest.loading());
        setLoggerLevel(instanceId!, logger.name, level)
            .then(() => {
                setUpdateLoggerLevel(StatelessRequest.success());
            })
            .catch(() => setUpdateLoggerLevel(StatelessRequest.error("")));
    };

    return (
        <div className={styles.MainWrapper}>
            <TooltipWithCopy text={logger.name} />

            <div className={styles.LoggerValuesWrapper}>
                <div className={styles.LevelsWrapper}>
                    {levels.map((level) => {
                        // @ts-expect-error todo fix type in future
                        const color = statePalette[level] || statePalette.DEFAULT;

                        return (
                            <div className={styles.RadioGroupWrapper} key={level}>
                                <label
                                    className={`${styles.RadioButton} ${effectiveLevel === level ? styles.Selected : ""}`}
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
                                        checked={effectiveLevel === level}
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
        </div>
    );
};
