import { useEffect, type ChangeEvent } from "react";
import { Empty, Input, Table } from "antd";
import { useTranslation } from "react-i18next";

import { filterConfigProps, getConfigPropsThunk } from "store/slices";
import { useAppDispatch, useAppSelector } from "hooks";
import type { ColumnsType } from "antd/es/table";
import type { IKeyValuePair } from "models";
import { Loader } from "components";

import styles from "./styles.module.css";

const createTableColumns = (
  title: string,
  prefix: string
): ColumnsType<IKeyValuePair> => {
  return [
    {
      title: (
        <>
          <div>{title}</div>
          <div className={styles.Prefix}>{prefix}</div>
        </>
      ),
      onHeaderCell: () => ({
        style: { backgroundColor: "#00AB551A" },
      }),
      render: (_, { key, value }) => (
        <>
          <span className={styles.TableRow}>{key}</span>
          <span className={styles.TableRow}>{value}</span>
        </>
      ),
    },
  ];
};

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

  return (
    <>
      <Input
        placeholder={t("search")}
        onChange={handleChange}
        className={styles.Search}
      />

      {configPropsSearchText && !filteredBeans.length ? (
        // todo В будущем, в зависимости от возможности переиспользования,
        // сделать один универсальный компонент Empty
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={<p>{t("noData")}</p>}
        />
      ) : (
        configProps.map(({ beanName, prefix, properties }) => {
          return (
            // todo В будущем, в зависимости от возможности переиспользования,
            // сделать один универсальный компонент Table
            <Table
              columns={createTableColumns(beanName, prefix)}
              dataSource={properties}
              pagination={false}
              key={beanName}
              className={styles.ConfigPropsTable}
            />
          );
        })
      )}
    </>
  );
};
