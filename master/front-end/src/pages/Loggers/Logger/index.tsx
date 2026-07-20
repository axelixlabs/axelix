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
import dayjs from "dayjs";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";

import { TooltipWithCopy } from "components";
import type { ILogger, TChangeLoggerLevel } from "models";
import { resetLogger, setLoggerLevel } from "services";

import { Levels } from "../Levels";

import { LoggerScheduler } from "./LoggerScheduler";
import styles from "./styles.module.css";

import { ResetIcon } from "assets";

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
    const { effectiveLevel, configuredLevel, name, temporaryLevelInitiatedAt, temporaryLevelRollsBackAt } = logger;
    const { instanceId } = useParams();

    const { message } = App.useApp();

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

        const updateTimer = (): void => {
            const remainingMilliseconds = endTime.diff(dayjs());

            if (remainingMilliseconds <= 0) {
                setRemainingTime(null);
                return;
            }

            const remainingSeconds = Math.floor(remainingMilliseconds / 1000);
            const hours = Math.floor(remainingSeconds / 3600);
            const minutes = Math.floor((remainingSeconds % 3600) / 60);

            setRemainingTime(`${hours}h ${minutes}m`);
        };

        updateTimer();

        const interval = setInterval(updateTimer, 60000);

        return () => {
            clearInterval(interval);
        };
    }, [temporaryLevelInitiatedAt, temporaryLevelRollsBackAt]);

    const handleChange: TChangeLoggerLevel = (level, ttlSeconds = null): void => {
        // if the ttl is specified, then this is almost 100% going to be different from the
        // one that is already configured. The only case that is no-op, is when we're configuring
        // the same logging level without ttl specified
        if (configuredLevel === level && ttlSeconds == null) {
            return;
        }

        setLoggerLevel({
            instanceIds: [instanceId!],
            loggerName: logger.name,
            configuredLevel: level,
            ttlSeconds: ttlSeconds,
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
        <div className={styles.MainWrapper}>
            <TooltipWithCopy text={name} />

            <div className={styles.LevelsWrapper}>
                <LoggerScheduler
                    checkedLevel={effectiveLevel}
                    handleChange={handleChange}
                    levels={levels}
                    remainingTime={remainingTime}
                />

                <Levels
                    checkedLevel={effectiveLevel}
                    configuredLevel={configuredLevel}
                    levels={levels}
                    handleChange={handleChange}
                    remainingTime={remainingTime}
                />

                <ResetIcon
                    className={`${styles.ResetIcon} ${!temporaryLevelInitiatedAt ? styles.DisabledResetIcon : ""}`}
                    onClick={temporaryLevelInitiatedAt ? () => handleLoggerReset(name) : undefined}
                />
            </div>
        </div>
    );
};
