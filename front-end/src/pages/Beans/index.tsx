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
  }, []);

  if (loading) {
    return <Loader />;
  }

  if (error) {
    // todo change error handling in future
    return error;
  }

  const noDataAfterSearch = !!beansSearchText && !filteredBeans.length;

  return (
    <>
      <PageSearch onChange={(value) => dispatch(filterBeans(value))} />

      <EmptyHandler isEmpty={noDataAfterSearch}>
        <BeansCollapse beans={filteredBeans.length ? filteredBeans : beans} />
      </EmptyHandler>
    </>
  );
};

export default Beans;