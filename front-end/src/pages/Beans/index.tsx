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

  const [effectiveBeans, setEffectiveBeans] = useState<IBean[]>(beans)

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

  const beansList = effectiveBeans
  const noSearchResults = !effectiveBeans.length;
  const addonAfter = `${effectiveBeans.length} / ${beans.length}`;

  const handleSearchChange = (search: string): void => {
    if (search) {
      setEffectiveBeans(filterBeans(beans, search));
    } else {
      setEffectiveBeans(beans);
    }
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