import { message } from "antd";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Loader, EmptyHandler, ModifiableTableSection, PageSearch } from "components";
import { fetchData, filterConfigPropsBeans } from "helpers";
import { type IConfigPropsBeanData, StatefulRequest } from "models";

import styles from "./styles.module.css";
import { getConfigPropsData } from "services";
import { useAppSelector } from "hooks";

export const ConfigProps = () => {
  const { t } = useTranslation()
  const { instanceId } = useParams()

  const [search, setSearch] = useState<string>("")
  const [configProps, setConfigProps] = useState(StatefulRequest.loading<IConfigPropsBeanData>())
  const updatePropertyState = useAppSelector(state => state.updateProperty)

  const fetchConfigProps = (instanceId: string) =>
    fetchData(setConfigProps, () => getConfigPropsData(instanceId))

  useEffect(() => {
    if (instanceId) {
      fetchConfigProps(instanceId)
    }
  }, []);

  useEffect(() => {
    if (updatePropertyState.completedSuccessfully() && instanceId) {
      fetchConfigProps(instanceId)
      message.success(t('saved'))
    }
  }, [updatePropertyState]);

  if (configProps.loading) {
    return <Loader />;
  }

  if (configProps.error) {
    // TODO: handle this case differently in the future
    return configProps.error;
  }

  const configPropsBeansFeed = configProps.response!.beans;

  const effectiveConfigProps = search
    ? filterConfigPropsBeans(configPropsBeansFeed, search)
    : configPropsBeansFeed;

  const addonAfter = `${effectiveConfigProps.length} / ${configPropsBeansFeed.length}`;

  return (
    <>
      <PageSearch addonAfter={addonAfter} onChange={(e) => setSearch(e)} />

      <EmptyHandler isEmpty={effectiveConfigProps.length === 0}>
        {effectiveConfigProps.map(({ beanName, prefix, properties }) => (
          <ModifiableTableSection
            headerName={beanName}
            properties={
              properties.map((property) => {
                return {
                  key: `${prefix}.${property.key}`,
                  displayKey: property.key,
                  displayValue: property.value,
                }
              })
            }

            key={beanName}
          >
            {prefix && (
              <div className={styles.Prefix}>
                <span className={styles.PrefixTitle}>Prefix:</span> {prefix}
              </div>
            )}
          </ModifiableTableSection>
        ))}
      </EmptyHandler>
    </>
  );
};

export default ConfigProps;