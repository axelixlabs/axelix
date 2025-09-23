import { Input, Table } from "antd";
import { useTranslation } from "react-i18next";
import type { ColumnsType } from "antd/es/table";

import type {
  IEnvironmantProperty,
  IEnvironmantPropertySource,
  TableData,
} from "../../../models";

import styles from "./styles.module.css";

const createTableData = (
  environmantProperies: IEnvironmantProperty[]
): TableData[] => {
  return environmantProperies.map(({ key, value }) => ({
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
  propertySources: IEnvironmantPropertySource[];
}

export const EnvironmantTables = ({ propertySources }: IProps) => {
  const { t } = useTranslation();

  return (
    <div className={styles.MainWrapper}>
      <Input placeholder={t("search")} className={styles.Search} />
      {propertySources.map(({ name, properties }) => (
        <Table
          columns={createTableColumns(name)}
          dataSource={createTableData(properties)}
          pagination={false}
          className={styles.EnvironmantTable}
        />
      ))}
    </div>
  );
};
