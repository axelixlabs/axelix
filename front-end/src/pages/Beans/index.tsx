import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import { Accordion, EmptyHandler, Loader, PageSearch } from "components";
import { fetchData, filterBeans } from "helpers";
import { type IBeansResponseBody, StatefulRequest } from "models";
import { getBeansData } from "services";

import { BeanAccordionChildren } from "./BeanAccordionChildren";
import { BeanAccordionLabels } from "./BeanAccordionLabels";

export const Beans = () => {
    const { instanceId } = useParams();

    const [dataState, setDataState] = useState(StatefulRequest.loading<IBeansResponseBody>());
    const [search, setSearch] = useState<string>("");
    const [activeKey, setActiveKey] = useState<string>("");

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
            <PageSearch addonAfter={addonAfter} setSearch={setSearch} />

            <EmptyHandler isEmpty={!effectiveBeans.length}>
                <div className="AccordionsWrapper">
                    {effectiveBeans.map((bean) => (
                        <Accordion
                            header={<BeanAccordionLabels bean={bean} />}
                            key={bean.beanName}
                            isActiveKey={activeKey === bean.beanName}
                        >
                            <BeanAccordionChildren bean={bean} setActiveKey={setActiveKey} />
                        </Accordion>
                    ))}
                </div>
            </EmptyHandler>
        </>
    );
};

export default Beans;
