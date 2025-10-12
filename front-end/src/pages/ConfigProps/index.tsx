import { message } from "antd";
import { useEffect } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { filterConfigProps, getConfigPropsThunk, resetChangePropertySuccess } from "store/slices";
import { Loader, EmptyHandler, ModifiableTableSection, PageSearch } from "components";
import { useAppDispatch, useAppSelector } from "hooks";

import styles from "./styles.module.css";

export const ConfigProps = () => {
  const { t } = useTranslation()
  const { instanceId } = useParams()

  const dispatch = useAppDispatch();

  const { beans, filteredBeans, configPropsSearchText, loading, error } = useAppSelector((store) => store.configProps);

  const { changePropertySuccess } = useAppSelector((store) => store.updateProperty);

  const fetchConfigProps = () => {
    if (instanceId) {
      dispatch(getConfigPropsThunk(instanceId));
    }
  };

  // todo So far, I haven't been able to find a way to combine the useEffects without causing an extra server request.
  useEffect(() => {
    fetchConfigProps()
  }, []);

  useEffect(() => {
    if (changePropertySuccess) {
      fetchConfigProps()
      message.success(t('saved'))
      dispatch(resetChangePropertySuccess());
    }
  }, [changePropertySuccess]);

  if (loading) {
    return <Loader />;
  }

  if (error) {
    return error;
  }

  const configProps = filteredBeans.length ? filteredBeans : beans;
  const noDataAfterSearch = !!configPropsSearchText && !filteredBeans.length;
  const addonAfter = `${configPropsSearchText ? filteredBeans.length : beans.length} / ${beans.length}`;

  return (
    <>
      <PageSearch addonAfter={addonAfter} onChange={(value) => dispatch(filterConfigProps(value))} />

      <EmptyHandler isEmpty={noDataAfterSearch}>
        {configProps.map(({ beanName, prefix, properties }) => (
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