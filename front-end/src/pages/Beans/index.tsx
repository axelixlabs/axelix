import { Input } from "antd";
import { useEffect } from "react";
import { useTranslation } from "react-i18next";

import { filterBeans, getBeansThunk } from "store/slices/beans";
import { useAppDispatch, useAppSelector } from "hooks";
import { Loader, EmptyHandler } from "components";
import { BeansCollapse } from "./BeansCollapse";

import styles from "./styles.module.css";

export const Beans = () => {
  const { t } = useTranslation();

  const dispatch = useAppDispatch();
  const { beans, filteredBeans, beansSearchText, loading, error } =
    useAppSelector((store) => store.beans);

  useEffect(() => {
      // todo В будущем вместо hard code-а вставить динамический id.
    dispatch(getBeansThunk("56019718-3b84-4ecd-9b84-287754dbd7d4"));
  }, [dispatch]);

  if (loading) {
    return <Loader />;
  }

  if (error) {
    // todo change error handling in future
    return error;
  }

  const noDataAfterSearch = !!beansSearchText && !filteredBeans.length;

  return (
    <>
      <Input
        placeholder={t("search")}
        onChange={(e) => dispatch(filterBeans(e.target.value))}
        className={styles.Search}
      />

      <EmptyHandler isEmpty={noDataAfterSearch}>
        <BeansCollapse beans={filteredBeans.length ? filteredBeans : beans} />
      </EmptyHandler>
    </>
  );
};
