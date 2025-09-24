import { Input, Table } from "antd";
import { useTranslation } from "react-i18next";
import type { ColumnsType } from "antd/es/table";

import type {
  IEnvironmentProperty,
  IEnvironmentPropertySource,
  TableData,
} from "models";

import styles from "./styles.module.css";

const createTableData = (
  environmentProperies: IEnvironmentProperty[]
): TableData[] => {
  return environmentProperies.map(({ key, value }) => ({
    key,
    value,
    name: key,
  }));
};

const createTableColumns = (title: string): ColumnsType<TableData> => {
  return [
    {
      title,
      onHeaderCell: () => ({
        style: { backgroundColor: "#00AB551A" },
      }),
      render: (_, { name, value }) => (
        <>
          <span className={styles.TableRow}>{name}</span>
          <span className={styles.TableRow}>{value}</span>
        </>
      ),
    },
  ];
};

interface IProps {
  /**
   *   The array of property sources (named-entities that hold a bundle of properties)
   *   that are available inside the given Spring Boot application
   */
  propertySources: IEnvironmentPropertySource[];
}

export const EnvironmentTables = ({ propertySources }: IProps) => {
  const { t } = useTranslation();

  return (
    <div className={styles.MainWrapper}>
      <Input placeholder={t("search")} className={styles.Search} />
      {propertySources.map(({ name, properties }) => (
        <Table
          columns={createTableColumns(name)}
          dataSource={createTableData(properties)}
          pagination={false}
          className={styles.EnvironmentTable}
        />
      ))}
    </div>
  );
};
