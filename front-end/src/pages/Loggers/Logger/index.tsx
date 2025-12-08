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
import type { AxiosError } from "axios";
import type { Dispatch, SetStateAction } from "react";
import { useParams } from "react-router-dom";

import { TooltipWithCopy } from "components";
import { extractErrorCode } from "helpers";
import { type IErrorResponse, type ILogger, StatelessRequest } from "models";
import { setLoggerLevel } from "services";

import { Levels } from "../Levels";

import styles from "./styles.module.css";

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
        <div className={styles.MainWrapper}>
            <TooltipWithCopy text={logger.name} />

            <Levels
                checkedLevel={effectiveLevel}
                configuredLevel={configuredLevel}
                levels={levels}
                handleChange={handleChange}
            />
        </div>
    );
};
