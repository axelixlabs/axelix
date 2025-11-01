import type { Dispatch, SetStateAction } from "react";
import { useParams } from "react-router-dom";

import { TooltipWithCopy } from "components";
import { type ILogger, StatelessRequest } from "models";
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
            .catch(() => setUpdateLoggerLevel(StatelessRequest.error("")));
    };

    return (
        <div className={styles.MainWrapper}>
            <TooltipWithCopy text={logger.name} />

            <Levels
                levelCheckEntity={effectiveLevel}
                configuredLevel={configuredLevel}
                levels={levels}
                handleChange={handleChange}
            />
        </div>
    );
};
