import { Input } from "antd";
import { useTranslation } from "react-i18next";
import { useEffect, type ChangeEvent } from "react";

import { filterConfigProps, getConfigPropsThunk } from "store/slices";
import { Loader, EmptyHandler, TableSection } from "components";
import { useAppDispatch, useAppSelector } from "hooks";

import styles from "./styles.module.css";

export const ConfigProps = () => {
  const { t } = useTranslation();

  const dispatch = useAppDispatch();
  const { beans, filteredBeans, configPropsSearchText, loading, error } = useAppSelector((store) => store.configProps);

  useEffect(() => {
    // todo В будущем вместо hard code-а вставить динамический id.
    dispatch(getConfigPropsThunk("56019718-3b84-4ecd-9b84-287754dbd7d4"));
  }, [dispatch]);

  if (loading) {
    return <Loader />;
  }

  if (error) {
    return error;
  }

  const configProps = filteredBeans.length ? filteredBeans : beans;

  const handleChange = (e: ChangeEvent<HTMLInputElement>): void => {
    dispatch(filterConfigProps(e.target.value));
  };

  const noDataAfterSearch = !!configPropsSearchText && !filteredBeans.length;

  return (
    <>
      <Input
        placeholder={t("search")}
        onChange={handleChange}
        className={styles.Search}
      />

      <EmptyHandler isEmpty={noDataAfterSearch}>
        {configProps.map(({ beanName, prefix, properties }) => (
          <TableSection
            name={beanName}
            properties={properties}
          >
            {prefix && (
              <div className={styles.Prefix}>
                <span className={styles.PrefixTitle}>Prefix:</span> {prefix}
              </div>
            )}
          </TableSection>
        ))}
      </EmptyHandler>
    </>
  );
};
