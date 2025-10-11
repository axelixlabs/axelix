import { message } from "antd";
import { useEffect } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { filterConfigProps, getConfigPropsThunk, resetChangePropertySuccess } from "store/slices";
import { Loader, EmptyHandler, TableSection, PageSearch } from "components";
import { useAppDispatch, useAppSelector } from "hooks";

import styles from "./styles.module.css";

export const ConfigProps = () => {
  const { t } = useTranslation()
  const { instanceId } = useParams()

  const dispatch = useAppDispatch();
  const { beans, filteredBeans, configPropsSearchText, loading, error } = useAppSelector((store) => store.configProps);
  const { changePropertySuccess, changePropertyloading } = useAppSelector((store) => store.updateProperty);

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

  if (loading || changePropertyloading) {
    return <Loader />;
  }

  if (error) {
    return error;
  }

  const configProps = filteredBeans.length ? filteredBeans : beans;
  const noDataAfterSearch = !!configPropsSearchText && !filteredBeans.length;

  return (
    <>
      <PageSearch onChange={(value) => dispatch(filterConfigProps(value))} />

      <EmptyHandler isEmpty={noDataAfterSearch}>
        {configProps.map(({ beanName, prefix, properties }) => (
          <TableSection
            headerName={beanName}
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