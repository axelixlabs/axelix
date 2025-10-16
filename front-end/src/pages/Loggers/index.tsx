import { message } from "antd";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Loader, EmptyHandler, PageSearch } from "components";
import { useAppDispatch, useAppSelector } from "hooks";
import { getLoggersThunk } from "store/thunks";
import { filterLoggers } from "helpers";
import type { ILogger } from "models";
import { Logger } from "./Logger";

export const Loggers = () => {
  const { t } = useTranslation();
  const { instanceId } = useParams();

  const dispatch = useAppDispatch();
  const { loading, updateLoggerSuccess, error, loggers, levels } = useAppSelector((store) => store.loggers);

  const [isSearched, setIsSearched] = useState<boolean>(false)
  const [filteredLoggers, setFilteredLoggers] = useState<ILogger[]>([])

  useEffect(() => {
    if (instanceId) {
      dispatch(getLoggersThunk(instanceId));
    }
  }, []);

  useEffect(() => {
    if (updateLoggerSuccess) {
      message.success(t("loggerLevelUpdated"))
    }
  }, [updateLoggerSuccess])

  if (loading) {
    return <Loader />;
  }

  if (error) {
    return error;
  }

  const noSearchResults = isSearched && !filteredLoggers.length;
  const addonAfter = `${isSearched ? filteredLoggers.length : loggers.length} / ${loggers.length}`;
  const loggersData = isSearched ? filteredLoggers : loggers;

  const handleSearchChange = (search: string): void => {
    const isSearching = Boolean(search);
    setIsSearched(isSearching);

    if (!isSearching) {
      setFilteredLoggers([]);
      return;
    }

    setFilteredLoggers(filterLoggers(loggers, search));
  };

  return (
    <>
      <PageSearch addonAfter={addonAfter} onChange={handleSearchChange} />

      <EmptyHandler isEmpty={noSearchResults}>
        {loggersData.map((logger) => (
          <Logger logger={logger} levels={levels} key={logger.name} />
        ))}
      </EmptyHandler>
    </>
  );
};

export default Loggers;