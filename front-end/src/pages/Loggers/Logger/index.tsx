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
import { App } from "antd";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { TooltipWithCopy } from "components";
import { type ILogger } from "models";
import { resetLogger, setLoggerLevel } from "services";

import { Levels } from "../Levels";

import styles from "./styles.module.css";

import { Reset } from "assets";

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
     * Fetches loggers data.
     */
    fetchLoggersData: () => void;
}

export const Logger = ({ levels, logger, fetchLoggersData }: IProps) => {
    // TODO: Add loading handler in future after fetchData and StatefulRequest refactoring
    const { t } = useTranslation();
    const { effectiveLevel, configuredLevel } = logger;
    const { instanceId } = useParams();

    const { message } = App.useApp();

    const handleChange = (level: string): void => {
        if (configuredLevel === level) {
            return;
        }

        setLoggerLevel({
            instanceId: instanceId!,
            loggerName: logger.name,
            loggingLevel: level,
        }).then(() => {
            message.success(t("Loggers.loggerLevelUpdated"));
            fetchLoggersData();
        });
    };

    const handleLoggerReset = (loggerName: string): void => {
        resetLogger({
            instanceId: instanceId!,
            loggerName: loggerName,
        }).then(() => {
            message.success(t("Loggers.reset"));
            fetchLoggersData();
        });
    };

    return (
        <>
            <div className={styles.MainWrapper}>
                <TooltipWithCopy text={logger.name} />

                <div className={styles.LevelsWrapper}>
                    <Levels
                        checkedLevel={effectiveLevel}
                        configuredLevel={configuredLevel}
                        levels={levels}
                        handleChange={handleChange}
                    />
                    <Reset className={styles.Reset} onClick={() => handleLoggerReset(logger.name)} />
                </div>
            </div>
        </>
    );
};
