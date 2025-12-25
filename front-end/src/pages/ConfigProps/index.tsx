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
import { message } from "antd";
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
