import { useEffect } from "react";
import { useParams } from "react-router-dom";

import { BeansCollapse } from "./BeansCollapse";
import { useAppDispatch, useAppSelector } from "hooks";
import { filterBeans, getBeansThunk } from "store/slices";
import { Loader, EmptyHandler, PageSearch } from "components";

export const Beans = () => {
  const { instanceId } = useParams();

  const dispatch = useAppDispatch();
  const { beans, filteredBeans, beansSearchText, loading, error } =
    useAppSelector((store) => store.beans);

  useEffect(() => {
    if (instanceId) {
      dispatch(getBeansThunk(instanceId));
    }
    // The dispatch passed as a dependency to useEffect does not affect its execution, since the dispatch function is never recreated.
    // There are two common approaches: either include dispatch in the dependencies or omit it. 
    // Both approaches are considered correct.
  }, [dispatch]);

  if (loading) {
    return <Loader />;
  }

  if (error) {
    // todo change error handling in future
    return error;
  }

  const noDataAfterSearch = !!beansSearchText && !filteredBeans.length;
  const addonAfter = `${beansSearchText ? filteredBeans.length : beans.length} / ${beans.length}`;
  const beansList = filteredBeans.length ? filteredBeans : beans

  return (
    <>
      <PageSearch addonAfter={addonAfter} onChange={(value) => dispatch(filterBeans(value))} />

      <EmptyHandler isEmpty={noDataAfterSearch}>
        <BeansCollapse beans={beansList} />
      </EmptyHandler>
    </>
  );
};

export default Beans;