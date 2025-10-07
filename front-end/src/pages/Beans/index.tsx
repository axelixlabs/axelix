import { Input } from "antd";
import { useEffect } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { filterBeans, getBeansThunk } from "store/slices/beans";
import { useAppDispatch, useAppSelector } from "hooks";
import { Loader, EmptyHandler } from "components";
import { BeansCollapse } from "./BeansCollapse";

import styles from "./styles.module.css";

export const Beans = () => {
  const { t } = useTranslation();
  const { instanceId } = useParams();

  const dispatch = useAppDispatch();
  const { beans, filteredBeans, beansSearchText, loading, error } =
    useAppSelector((store) => store.beans);

  useEffect(() => {
    if (instanceId) {
      dispatch(getBeansThunk(instanceId));
    }
    // The dispatch passed as a dependency to useEffect does not affect its execution, since the dispatch function is never recreated.
    // There are two common approaches: either include dispatch in the dependencies or omit it. 
    // Both approaches are considered correct.
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

export default Beans;