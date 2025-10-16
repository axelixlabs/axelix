import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import { Loader, EmptyHandler, PageSearch } from "components";
import { useAppDispatch, useAppSelector } from "hooks";
import { BeansCollapse } from "./BeansCollapse";
import { getBeansThunk } from "store/thunks";
import { filterBeans } from "helpers";
import type { IBean } from "models";

export const Beans = () => {
  const { instanceId } = useParams();

  const dispatch = useAppDispatch();
  const { beans, loading, error } = useAppSelector((store) => store.beans);

  const [isSearched, setIsSearched] = useState<boolean>(false)
  const [filteredBeans, setFilteredBeans] = useState<IBean[]>([])

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

  const beansList = isSearched ? filteredBeans : beans
  const noSearchResults = isSearched && !filteredBeans.length;
  const addonAfter = `${isSearched ? filteredBeans.length : beans.length} / ${beans.length}`;

  const handleSearchChange = (search: string): void => {
    const isSearching = Boolean(search);
    setIsSearched(isSearching);

    if (!isSearching) {
      setFilteredBeans([]);
      return;
    }

    setFilteredBeans(filterBeans(beans, search));
  };

  return (
    <>
      <PageSearch addonAfter={addonAfter} onChange={handleSearchChange} />

      <EmptyHandler isEmpty={noSearchResults}>
        <BeansCollapse beans={beansList} />
      </EmptyHandler>
    </>
  );
};

export default Beans;