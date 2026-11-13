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

import { EmptyHandler, HashNavigable, Loader, PageSearch } from "@/components";
import { fetchData, filterConfigPropsBeans, getPropertiesCount } from "@/helpers";
import { EPropertyTriageTag, type IConfigPropsBean, type IConfigPropsResponseBody, StatefulRequest } from "@/models";
import { getConfigPropsData } from "@/services";

import { PropertyTriageFilters } from "../Environment/EnvironmentTables/EnvironmentTriageFilters";
import toolbarStyles from "../Environment/EnvironmentTables/styles.module.css";

import { ConfigPropsTables } from "./ConfigPropsTables";

const ConfigProps = () => {
    const { instanceId } = useParams();

    const [triageTags, setTriageTags] = useState<EPropertyTriageTag[]>([]);
    const [search, setSearch] = useState<string>("");
    const [configProps, setConfigProps] = useState(StatefulRequest.loading<IConfigPropsResponseBody>());

    const fetchConfigProps = (instanceId: string) => fetchData(setConfigProps, () => getConfigPropsData(instanceId));

    useEffect(() => {
        if (instanceId) {
            fetchConfigProps(instanceId);
        }
    }, []);

    if (configProps.loading) {
        return <Loader />;
    }

    if (configProps.error) {
        return <EmptyHandler isEmpty />;
    }

    const configPropsBeansFeed = configProps.response!.beans;

    const dangerousOnly = triageTags.includes(EPropertyTriageTag.DANGEROUS);
    const effectiveConfigProps =
        search || dangerousOnly
            ? filterConfigPropsBeans(configPropsBeansFeed, search, dangerousOnly)
            : configPropsBeansFeed;
    const dangerousCount = configPropsBeansFeed.reduce(
        (count, bean) => count + bean.properties.filter((property) => !!property.dangerousValue).length,
        0,
    );

    const totalPropertiesCount = getPropertiesCount<IConfigPropsBean>(configPropsBeansFeed);
    const filteredPropertiesCount = getPropertiesCount<IConfigPropsBean>(effectiveConfigProps);

    const addonAfter = `${filteredPropertiesCount} / ${totalPropertiesCount}`;

    return (
        <>
            <div className={toolbarStyles.MainWrapper}>
                <PageSearch addonAfter={addonAfter} setSearch={setSearch} />
                <PropertyTriageFilters
                    counts={[{ triageTag: EPropertyTriageTag.DANGEROUS, count: dangerousCount }]}
                    selectedTags={triageTags}
                    onSelectedTagsChange={setTriageTags}
                />
            </div>
            <HashNavigable>
                <ConfigPropsTables effectiveConfigProps={effectiveConfigProps} />
            </HashNavigable>
        </>
    );
};

export default ConfigProps;
