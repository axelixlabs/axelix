import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import { Loader, EmptyHandler, PageSearch } from "components";
import { BeansCollapse } from "./BeansCollapse";
import { getBeansData } from "services";
import { fetchData, filterBeans } from "helpers";
import { StatefulRequest, type BeansResponse } from "models";

export const Beans = () => {
  const { instanceId } = useParams();

  const [dataState, setDataState] = useState(StatefulRequest.loading<BeansResponse>())
  const [search, setSearch] = useState<string>("");

  useEffect(() => {
    fetchData(setDataState, () => getBeansData(instanceId!))
  }, []);

  if (dataState.loading) {
    return <Loader />;
  }

  if (dataState.error) {
    // todo change error handling in future
    return dataState.error;
  }

  const beansFeed = dataState.response!.beans;
  const effectiveBeans = search ? filterBeans(beansFeed, search) : beansFeed;
  const addonAfter = `${effectiveBeans.length} / ${beansFeed.length}`;

  return (
    <>
      <PageSearch addonAfter={addonAfter} search={search} setSearch={setSearch} />

      <EmptyHandler isEmpty={!effectiveBeans.length}>
        <BeansCollapse beans={effectiveBeans} />
      </EmptyHandler>
    </>
  );
};

export default Beans;