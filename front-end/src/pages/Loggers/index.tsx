import { message } from "antd";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Loader, EmptyHandler, PageSearch } from "components";
import { fetchData, filterLoggers } from "helpers";
import {type ILoggerData, type ILoggersSliceState, StatefulRequest} from "models";
import { Logger } from "./Logger";
import { getLoggersData } from "services";
import {useAppSelector} from "hooks";

export const Loggers = () => {
  const { t } = useTranslation();
  const { instanceId } = useParams();

  const [loggersData, setLoggersData] = useState(StatefulRequest.loading<ILoggerData>())
  const [search, setSearch] = useState<string>("")
  const sliceState : ILoggersSliceState = useAppSelector(state => state.loggers)

  const fetchLoggersData = (instanceId: string) =>
    fetchData(setLoggersData, () => getLoggersData(instanceId));

  useEffect(() => {
    if (instanceId) {
      fetchLoggersData(instanceId);
    }
  }, [sliceState]);

  useEffect(() => {
    if (sliceState.updateLoggerSuccess) {
      message.success(t("loggerLevelUpdated"))
    }
  }, [sliceState])

  if (loggersData.loading || sliceState.loading) {
    return <Loader />;
  }

  if (loggersData.error) {
    return loggersData.error;
  }

  const loggers = loggersData.response!.loggers;
  const effectiveLoggers = search ? filterLoggers(loggers, search) : loggers
  const addonAfter = `${effectiveLoggers.length} / ${loggers.length}`

  return (
    <>
      <PageSearch addonAfter={addonAfter} onChange={(e) => setSearch(e)} />

      <EmptyHandler isEmpty={effectiveLoggers.length === 0}>
        {effectiveLoggers.map((logger) => (
          <Logger logger={logger} levels={loggersData.response!.levels} key={logger.name} />
        ))}
      </EmptyHandler>
    </>
  );
};

export default Loggers;