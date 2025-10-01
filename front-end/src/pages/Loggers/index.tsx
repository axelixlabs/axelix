import { Input } from "antd";
import { useEffect } from "react";
import { useTranslation } from "react-i18next";

import { filterLoggers, getLoggersThunk } from "store/slices";
import { useAppDispatch, useAppSelector } from "hooks";
import { Loader, EmptyHandler } from "components";
import { LoggersList } from "./LoggersList";

import styles from "./styles.module.css";

export const Loggers = () => {
  const { t } = useTranslation();

  const dispatch = useAppDispatch();
  const { loading, error, loggers, filteredLoggers, loggersSearchText } =
    useAppSelector((store) => store.loggers);

  useEffect(() => {
    // todo remove "" in future
    dispatch(getLoggersThunk(""));
  }, [dispatch]);

  if (loading) {
    return <Loader />;
  }

  if (error) {
    return error;
  }

  const noDataAfterSearch = !!loggersSearchText && !filteredLoggers.length;

  const loggersCount = `${
    loggersSearchText ? filteredLoggers.length : loggers.length
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
