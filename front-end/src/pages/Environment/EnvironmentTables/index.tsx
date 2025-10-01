import { Empty, Input, Table } from "antd";
import { useTranslation } from "react-i18next";
import type { ColumnsType } from "antd/es/table";

import { useAppDispatch, useAppSelector } from "hooks";
import { filterEnvironments } from "store/slices";
import type { IKeyValuePair } from "models";

import styles from "./styles.module.css";

const createTableColumns = (title: string): ColumnsType<IKeyValuePair> => {
  return [
    {
      title,
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

export const EnvironmentTables = () => {
  const { t } = useTranslation();

  const dispatch = useAppDispatch();

  const { propertySources, filteredEnvironments, environmentSearchText } =
    useAppSelector((store) => store.environment);

  const propertySourcesList = filteredEnvironments.length
    ? filteredEnvironments
    : propertySources;

  return (
    <>
      <Input
        placeholder={t("search")}
        onChange={(e) => dispatch(filterEnvironments(e.target.value))}
        className={styles.Search}
      />

      {/* todo - replace this in future in EmptyHandler component*/}
      {environmentSearchText && !filteredEnvironments.length ? (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={<p>{t("noData")}</p>}
        />
      ) : (
        propertySourcesList.map(({ name, properties }) => {
          return (
            <Table
              columns={createTableColumns(name)}
              dataSource={properties}
              pagination={false}
              key={name}
              className={styles.EnvironmentTable}
            />
          );
        })
      )}
    </>
  );
};
