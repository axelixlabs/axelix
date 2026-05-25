/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import { useEffect, useState } from "react";
import { useParams, useSearchParams } from "react-router";

import { EmptyHandler, Loader } from "components";
import { fetchData, getEffectiveBeans } from "helpers";
import { type IBeansResponseBody, StatefulRequest } from "models";
import { getBeansData } from "services";

import { BeansAccordionsList } from "./BeansAccordionsList";
import { BeansFirstSection } from "./BeansFirstSection";

const Beans = () => {
    const { instanceId } = useParams();
    const [searchParams, setSearchParams] = useSearchParams();

    const [dataState, setDataState] = useState(StatefulRequest.loading<IBeansResponseBody>());
    const [search, setSearch] = useState<string>("");

    const selectedBeanName = searchParams.get("name");
    const selectBean = (beanNameToSelect: string | null) => {
        setSearchParams(
            beanNameToSelect
                ? {
                      name: beanNameToSelect,
                  }
                : new URLSearchParams(), // simulating the removal of the name parameter
        );
    };

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
    const effectiveBeans = getEffectiveBeans(selectedBeanName, search, beansFeed);
    const addonAfter = `${effectiveBeans.length} / ${beansFeed.length}`;

    return (
        <>
            <BeansFirstSection
                addonAfter={addonAfter}
                setSearch={setSearch}
                selectBean={selectBean}
                selectedBeanName={selectedBeanName}
            />

            <EmptyHandler isEmpty={!effectiveBeans.length}>
                <BeansAccordionsList
                    effectiveBeans={effectiveBeans}
                    beansFeed={beansFeed}
                    selectedBeanName={selectedBeanName}
                    selectBean={selectBean}
                />
            </EmptyHandler>
        </>
    );
};

export default Beans;
