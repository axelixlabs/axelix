import { message } from "antd";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Loader, EmptyHandler, ModifiableTableSection, PageSearch } from "components";
import { resetChangePropertySuccess } from "store/slices";
import { useAppDispatch, useAppSelector } from "hooks";
import { getConfigPropsThunk } from "store/thunks";
import { filterConfigPropsBeans } from "helpers";
import type { IConfigPropsBean } from "models";

import styles from "./styles.module.css";

export const ConfigProps = () => {
  const { t } = useTranslation()
  const { instanceId } = useParams()

  const dispatch = useAppDispatch();

  const { beans, loading, error } = useAppSelector((store) => store.configProps);
  const { changePropertySuccess } = useAppSelector((store) => store.updateProperty);

  const [isSearched, setIsSearched] = useState<boolean>(false)
  const [filteredBeans, setFilteredBeans] = useState<IConfigPropsBean[]>([])

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

  const beansList = isSearched ? filteredBeans : beans;
  const noSearchResults = isSearched && !filteredBeans.length;
  const addonAfter = `${isSearched ? filteredBeans.length : beans.length} / ${beans.length}`;

  const handleSearchChange = (search: string): void => {
    const isSearching = Boolean(search);
    setIsSearched(isSearching);

    if (!isSearching) {
      setFilteredBeans([]);
      return;
    }

    setFilteredBeans(filterConfigPropsBeans(beans, search));
  };

  return (
    <>
      <PageSearch addonAfter={addonAfter} onChange={handleSearchChange} />

      <EmptyHandler isEmpty={noSearchResults}>
        {beansList.map(({ beanName, prefix, properties }) => (
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