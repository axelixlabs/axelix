import { Input } from "antd";
import { useTranslation } from "react-i18next";

import { useAppDispatch, useAppSelector } from "hooks";
import { EmptyHandler, TableData } from "components";
import { filterProperties } from "store/slices";

import styles from "./styles.module.css";

export const EnvironmentTables = () => {
  const { t } = useTranslation();

  const dispatch = useAppDispatch();

  const { propertySources, filteredPropertySources, environmentSearchText } =
    useAppSelector((store) => store.environment);

  const propertySourcesList = filteredPropertySources.length
    ? filteredPropertySources
    : propertySources;

  const noDataAfterSearch = !!environmentSearchText && !filteredPropertySources.length

  return (
    <>
      <Input
        placeholder={t("search")}
        onChange={(e) => dispatch(filterProperties(e.target.value))}
        className={styles.Search}
      />

      <EmptyHandler isEmpty={noDataAfterSearch}>
        {propertySourcesList.map(({ name, properties }) => (
          <TableData
            name={name}
            properties={properties}
            key={name}
          />
        ))}
      </EmptyHandler>
    </>
  );
};
