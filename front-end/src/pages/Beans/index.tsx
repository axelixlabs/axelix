import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader, PageSearch } from "components";
import { fetchData, filterBeans } from "helpers";
import { type BeansResponse, StatefulRequest } from "models";
import { getBeansData } from "services";

import { BeansCollapse } from "./BeansCollapse";

export const Beans = () => {
    const { instanceId } = useParams();

    const [dataState, setDataState] = useState(StatefulRequest.loading<BeansResponse>());
    const [search, setSearch] = useState<string>("");

    useEffect(() => {
        fetchData(setDataState, () => getBeansData(instanceId!));
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
