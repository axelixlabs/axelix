import { Input, Table } from "antd";
import { useTranslation } from "react-i18next";
import type { ColumnsType } from "antd/es/table";

import type { IEnvironmentPropertySource, IKeyValuePair } from "models";

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
    <>
      <Input placeholder={t("search")} className={styles.Search} />
      {propertySources.map(({ name, properties }) => {
        return (
          <Table
            columns={createTableColumns(name)}
            dataSource={properties}
            pagination={false}
            key={name}
            className={styles.EnvironmentTable}
          />
        );
      })}
    </>
  );
};
