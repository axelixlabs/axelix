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
import { useParams } from "react-router";

import { EmptyHandler, Loader } from "components";
import { fetchData, getEffectiveBeans } from "helpers";
import { type IBean, type IBeansResponseBody, StatefulRequest } from "models";
import { getBeansData } from "services";

import { BeansAccordionsList } from "./BeansAccordionsList";
import { BeansFirstSection } from "./BeansFirstSection";

const Beans = () => {
    const { instanceId } = useParams();

    const [dataState, setDataState] = useState(StatefulRequest.loading<IBeansResponseBody>());
    const [search, setSearch] = useState<string>("");
    const [selectedBean, setSelectedBean] = useState<IBean | null>(null);

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
    const effectiveBeans = getEffectiveBeans(selectedBean, search, beansFeed);
    const addonAfter = `${effectiveBeans.length} / ${beansFeed.length}`;

    return (
        <>
            <BeansFirstSection
                addonAfter={addonAfter}
                setSearch={setSearch}
                setSelectedBean={setSelectedBean}
                selectedBean={selectedBean}
            />

            <EmptyHandler isEmpty={!effectiveBeans.length}>
                <BeansAccordionsList
                    effectiveBeans={effectiveBeans}
                    beansFeed={beansFeed}
                    selectedBean={selectedBean}
                    setSelectedBean={setSelectedBean}
                />
            </EmptyHandler>
        </>
    );
};

export default Beans;
