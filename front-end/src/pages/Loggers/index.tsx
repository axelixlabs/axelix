import { useEffect } from "react";
import { Input, message } from "antd";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { filterLoggers, getLoggersThunk } from "store/slices";
import { useAppDispatch, useAppSelector } from "hooks";
import { Loader, EmptyHandler } from "components";
import { LoggersList } from "./LoggersList";

import styles from "./styles.module.css";

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

  const loggersCount = `${loggersSearchText ? filteredLoggers.length : loggers.length
    } / ${loggers.length}`;

  return (
    <>
      <Input
        placeholder={t("search")}
        onChange={(e) => dispatch(filterLoggers(e.target.value))}
        addonAfter={loggersCount}
        className={styles.Search}
      />

      <EmptyHandler isEmpty={noDataAfterSearch}>
        <LoggersList />
      </EmptyHandler>
    </>
  );
};

export default Loggers;