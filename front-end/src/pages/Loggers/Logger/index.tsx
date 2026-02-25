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
import type { AxiosError } from "axios";
import type { Dispatch, MouseEvent, SetStateAction } from "react";
import { useParams } from "react-router-dom";

import { TooltipWithCopy } from "components";
import { extractErrorCode } from "helpers";
import { type IErrorResponse, type ILogger, StatelessRequest } from "models";
import { setLoggerLevel } from "services";

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
     * setState to update the logger level
     */
    setUpdateLoggerLevel: Dispatch<SetStateAction<StatelessRequest>>;

    /**
     * The function to handle the reset of this given logger
     */
    handleReset: (_: MouseEvent, loggerName: string) => void;
}

export const Logger = ({ levels, logger, setUpdateLoggerLevel, handleReset }: IProps) => {
    const { effectiveLevel, configuredLevel } = logger;
    const { instanceId } = useParams();

    const handleChange = (level: string): void => {
        if (configuredLevel === level) {
            return;
        }

        setUpdateLoggerLevel(StatelessRequest.loading());
        setLoggerLevel({
            instanceId: instanceId!,
            loggerName: logger.name,
            loggingLevel: level,
        })
            .then(() => {
                setUpdateLoggerLevel(StatelessRequest.success());
            })
            .catch((error: AxiosError<IErrorResponse>) => {
                setUpdateLoggerLevel(StatelessRequest.error(extractErrorCode(error?.response?.data)));
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
                    <Reset className={styles.Reset} onClick={(e) => handleReset(e, logger.name)} color="#FF000AFF" />
                </div>
            </div>
        </>
    );
};
