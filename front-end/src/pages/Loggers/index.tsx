import { useEffect } from "react";
import { message } from "antd";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { filterLoggers, getLoggersThunk } from "store/slices";
import { useAppDispatch, useAppSelector } from "hooks";
import { Loader, EmptyHandler, PageSearch } from "components";
import { LoggersList } from "./LoggersList";

export const Loggers = () => {
  const { t } = useTranslation();
  const { instanceId } = useParams();

  const dispatch = useAppDispatch();
  const {
    loading,
    updateLoggerSuccess,
    error,
    loggers,
    filteredLoggers,
    loggersSearchText
  } = useAppSelector((store) => store.loggers);

  useEffect(() => {
    if (instanceId) {
      dispatch(getLoggersThunk(instanceId));
    }
    // The dispatch passed as a dependency to useEffect does not affect its execution, since the dispatch function is never recreated.
    // There are two common approaches: either include dispatch in the dependencies or omit it. 
    // Both approaches are considered correct.
  }, [dispatch]);

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

  const noDataAfterSearch = !!loggersSearchText && !filteredLoggers.length;
  const addonAfter = `${loggersSearchText ? filteredLoggers.length : loggers.length} / ${loggers.length}`;

  return (
    <>
      <PageSearch addonAfter={addonAfter} onChange={(value) => dispatch(filterLoggers(value))} />

      <EmptyHandler isEmpty={noDataAfterSearch}>
        <LoggersList />
      </EmptyHandler>
    </>
  );
};

export default Loggers;