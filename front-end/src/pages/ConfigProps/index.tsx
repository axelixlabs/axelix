import { useEffect } from "react";
import { useParams } from "react-router-dom";

import { Loader, EmptyHandler, TableSection, PageSearch } from "components";
import { filterConfigProps, getConfigPropsThunk } from "store/slices";
import { useAppDispatch, useAppSelector } from "hooks";

import styles from "./styles.module.css";

export const ConfigProps = () => {
  const { instanceId } = useParams()

  const dispatch = useAppDispatch();
  const { beans, filteredBeans, configPropsSearchText, loading, error } = useAppSelector((store) => store.configProps);

  useEffect(() => {
    if (instanceId) {
      dispatch(getConfigPropsThunk(instanceId));
    }
    // The dispatch passed as a dependency to useEffect does not affect its execution, since the dispatch function is never recreated.
    // There are two common approaches: either include dispatch in the dependencies or omit it. 
    // Both approaches are considered correct.
  }, [dispatch]);

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
      <PageSearch onChange={(value) => dispatch(filterConfigProps(value))} />

      <EmptyHandler isEmpty={noDataAfterSearch}>
        {configProps.map(({ beanName, prefix, properties }) => (
          <TableSection
            name={beanName}
            properties={properties}
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