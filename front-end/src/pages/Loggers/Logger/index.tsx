import { Tooltip } from "antd";
import { useTranslation } from "react-i18next";

import { setLoggerLevelThunk } from "store/slices";
import { TooltipWithCopy } from "components";
import { useAppDispatch } from "hooks";
import type { ILogger } from "models";
import { statePalette } from "utils";

import TargetIcon from 'assets/icons/TargetIcon.svg'

import styles from "./styles.module.css";

interface IProps {
  /**
   * Levels for all loggers
  */
  levels: string[];
  /**
   * Single logger
   */
  logger: ILogger;
}

export const Logger = ({ levels, logger }: IProps) => {
  const { t } = useTranslation();
  const dispatch = useAppDispatch();
  const { effectiveLevel, configuredLevel } = logger

  const handleChange = (level: string): void => {
    if (configuredLevel === level) {
      return
    }

    dispatch(setLoggerLevelThunk(level));
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
              <div className={styles.RadioGroupWrapper}>
                <label
                  key={level}
                  className={`${styles.RadioButton} ${effectiveLevel === level ? styles.Selected : ""}`}
                  onClick={() => handleChange(level)}
                  style={{
                    "--color-primary": color.colorPrimary,
                    "--color-primary-hover": color.colorPrimaryHover,
                    "--color-primary-active": color.colorPrimaryActive,
                  } as React.CSSProperties}
                >
                  <input
                    type="radio"
                    value={level}
                    checked={effectiveLevel === level}
                  />
                  {level}
                </label>
                {configuredLevel === level && (
                  <Tooltip title={t("configuredExplicitly")} className={styles.Tooltip}>
                    <img src={TargetIcon} alt="Target icon" className={styles.TargetIcon} />
                  </Tooltip>
                )}
              </div>
            );
          })}
        </div>
        <button className={styles.Reset}>
          {t("reset")}
        </button>
      </div>
    </div>
  );
};
