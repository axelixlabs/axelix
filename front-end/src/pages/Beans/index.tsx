/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import { EmptyHandler, HashNavigable, Loader, PageSearch } from "components";
import { fetchData, filterBeans } from "helpers";
import { type IBeansResponseBody, StatefulRequest } from "models";
import { getBeansData } from "services";

import { BeansAccordionsList } from "./BeansAccordionsList";

const Beans = () => {
    const { instanceId } = useParams();

    const [dataState, setDataState] = useState(StatefulRequest.loading<IBeansResponseBody>());
    const [search, setSearch] = useState<string>("");

    useEffect(() => {
        fetchData(setDataState, () => getBeansData(instanceId!));
    }, []);

    if (dataState.loading) {
        return <Loader />;
    }

    if (dataState.error) {
        return <EmptyHandler isEmpty />;
    }

    const beansFeed = dataState.response!.beans;
    const effectiveBeans = search ? filterBeans(beansFeed, search) : beansFeed;
    const addonAfter = `${effectiveBeans.length} / ${beansFeed.length}`;

    return (
        <>
            <PageSearch addonAfter={addonAfter} setSearch={setSearch} />

            <EmptyHandler isEmpty={!effectiveBeans.length}>
                <HashNavigable>
                    <BeansAccordionsList effectiveBeans={effectiveBeans} />
                </HashNavigable>
            </EmptyHandler>
        </>
    );
};

export default Beans;
