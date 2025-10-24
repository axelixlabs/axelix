import { message } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader, PageSearch } from "components";
import { fetchData, filterLoggers } from "helpers";
import { useAppSelector } from "hooks";
import { type ILoggerData, type ILoggersSliceState, StatefulRequest } from "models";
import { getLoggersData } from "services";

import { Logger } from "./Logger";

export const Loggers = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();

    const [loggersData, setLoggersData] = useState(StatefulRequest.loading<ILoggerData>());
    const [search, setSearch] = useState<string>("");
    const sliceState: ILoggersSliceState = useAppSelector((state) => state.loggers);

    const fetchLoggersData = (instanceId: string) => fetchData(setLoggersData, () => getLoggersData(instanceId));

    useEffect(() => {
        if (instanceId) {
            fetchLoggersData(instanceId);
        }
    }, [sliceState]);

    useEffect(() => {
        if (sliceState.updateLoggerSuccess) {
            message.success(t("Loggers.loggerLevelUpdated"));
        }
    }, [sliceState]);

    if (loggersData.loading || sliceState.loading) {
        return <Loader />;
    }

    if (loggersData.error) {
        return loggersData.error;
    }

    const loggers = loggersData.response!.loggers;
    const effectiveLoggers = search ? filterLoggers(loggers, search) : loggers;
    const addonAfter = `${effectiveLoggers.length} / ${loggers.length}`;

    return (
        <>
            <PageSearch addonAfter={addonAfter} search={search} setSearch={setSearch} />

            <EmptyHandler isEmpty={effectiveLoggers.length === 0}>
                {effectiveLoggers.map((logger) => (
                    <Logger logger={logger} levels={loggersData.response!.levels} key={logger.name} />
                ))}
            </EmptyHandler>
        </>
    );
};

export default Loggers;
