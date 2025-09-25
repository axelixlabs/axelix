import { useEffect } from "react";
import { Empty, Input } from "antd";
import { useTranslation } from "react-i18next";

import { filterBeans, getBeansThunk } from "store/slices/beans";
import { useAppDispatch, useAppSelector } from "hooks";
import { BeansCollapse } from "./BeansCollapse";

import styles from "./styles.module.css";

export const Beans = () => {
  const { t } = useTranslation();

  const dispatch = useAppDispatch();
  const { beans, filteredBeans, beansSearchText, loading, error } =
    useAppSelector((store) => store.beans);

  useEffect(() => {
    // todo remove this "1" in future
    dispatch(getBeansThunk("1"));
  }, [dispatch]);

  if (loading) {
    // todo create component for loading in future
    return "Loading...";
  }

  if (error) {
    // todo change error handling in future
    return error;
  }

  return (
    <>
      <Input
        placeholder={t("search")}
        onChange={(e) => dispatch(filterBeans(e.target.value))}
        className={styles.Search}
      />

      {beansSearchText && !filteredBeans.length ? (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={<p>{t("noData")}</p>}
        />
      ) : (
        <BeansCollapse beans={filteredBeans.length ? filteredBeans : beans} />
      )}
    </>
  );
};
