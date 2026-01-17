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
import { App } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, HashNavigable, Loader, PageSearch } from "components";
import { fetchData, filterConfigPropsBeans, getPropertiesCount } from "helpers";
import { useAppSelector } from "hooks";
import { type IConfigPropsBean, type IConfigPropsResponseBody, StatefulRequest } from "models";
import { getConfigPropsData } from "services";

import { ConfigPropsTables } from "./ConfigPropsTables";

const ConfigProps = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const { message } = App.useApp();

    const [search, setSearch] = useState<string>("");
    const [configProps, setConfigProps] = useState(StatefulRequest.loading<IConfigPropsResponseBody>());
    const updatePropertyState = useAppSelector((state) => state.updateProperty);

    const fetchConfigProps = (instanceId: string) => fetchData(setConfigProps, () => getConfigPropsData(instanceId));

    useEffect(() => {
        if (instanceId) {
            fetchConfigProps(instanceId);
        }
    }, []);

    useEffect(() => {
        if (updatePropertyState.completedSuccessfully()) {
            fetchConfigProps(instanceId!);
            message.success(t("saved"));
        }
    }, [updatePropertyState]);

    if (configProps.loading) {
        return <Loader />;
    }

    if (configProps.error) {
        return <EmptyHandler isEmpty />;
    }

    const configPropsBeansFeed = configProps.response!.beans;

    const effectiveConfigProps = search ? filterConfigPropsBeans(configPropsBeansFeed, search) : configPropsBeansFeed;

    const totalPropertiesCount = getPropertiesCount<IConfigPropsBean>(configPropsBeansFeed);
    const filteredPropertiesCount = getPropertiesCount<IConfigPropsBean>(effectiveConfigProps);

    const addonAfter = `${filteredPropertiesCount} / ${totalPropertiesCount}`;

    return (
        <>
            <PageSearch addonAfter={addonAfter} setSearch={setSearch} />
            <HashNavigable>
                <ConfigPropsTables effectiveConfigProps={effectiveConfigProps} />
            </HashNavigable>
        </>
    );
};

export default ConfigProps;
