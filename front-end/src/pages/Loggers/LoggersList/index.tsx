import type { Dispatch, SetStateAction } from "react";

import type { ILogger, StatelessRequest } from "models";

import { Logger } from "../Logger";

interface IProps {
    /**
     * Logger list
     */
    effectiveLoggers: ILogger[];
    /**
     * All possible logging levels that are supported by the logging system inside the instance
     */
    levels: string[];
    /**
     * State responsible for updating the logger level
     */
    setUpdateLoggerLevel: Dispatch<SetStateAction<StatelessRequest>>;
}

export const LoggersList = ({ effectiveLoggers, levels, setUpdateLoggerLevel }: IProps) => {
    return (
        <>
            {effectiveLoggers.map((logger) => (
                <Logger logger={logger} levels={levels} key={logger.name} setUpdateLoggerLevel={setUpdateLoggerLevel} />
            ))}
        </>
    );
};
