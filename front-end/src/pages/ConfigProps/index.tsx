import { useEffect } from "react";
import { useParams } from "react-router-dom";

import { Loader, EmptyHandler, TableSection, PageSearch } from "components";
import { filterConfigProps, getConfigPropsThunk, resetChangePropertySuccess } from "store/slices";
import { useAppDispatch, useAppSelector } from "hooks";

import styles from "./styles.module.css";
import { message } from "antd";
import { useTranslation } from "react-i18next";

export const ConfigProps = () => {
  const { instanceId } = useParams()
  const [messageApi, contextHolder] = message.useMessage();
  const { t } = useTranslation()

  const dispatch = useAppDispatch();
  const { beans, filteredBeans, configPropsSearchText, loading, error } = useAppSelector((store) => store.configProps);
  const { changePropertySuccess } = useAppSelector((store) => store.updateProperty);

  useEffect(() => {
    if (instanceId) {
      dispatch(getConfigPropsThunk(instanceId));
    }
  }, []);

  useEffect(() => {
    if (changePropertySuccess) {
      messageApi.success(t('saved'))
      dispatch(resetChangePropertySuccess())
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

  return (
    <>
      {contextHolder}
      <PageSearch onChange={(value) => dispatch(filterConfigProps(value))} />

      <EmptyHandler isEmpty={noDataAfterSearch}>
        {configProps.map(({ beanName, prefix, properties }) => (
          <TableSection
            name={beanName}
            properties={properties}
            key={beanName}
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

export default ConfigProps;