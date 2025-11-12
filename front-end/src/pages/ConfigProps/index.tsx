import { message } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { Loader, PageSearch } from "components";
import { fetchData, filterConfigPropsBeans, getPropertiesCount } from "helpers";
import { useAppSelector } from "hooks";
import { type IConfigPropsBean, type IConfigPropsResponseBody, StatefulRequest } from "models";
import { getConfigPropsData } from "services";

import { ConfigPropsTables } from "./ConfigPropsTables";

export const ConfigProps = () => {
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
        if (updatePropertyState.completedSuccessfully() && instanceId) {
            fetchConfigProps(instanceId);
            message.success(t("saved"));
        }
    }, [updatePropertyState]);

    if (configProps.loading) {
        return <Loader />;
    }

    if (configProps.error) {
        // TODO: handle this case differently in the future
        return configProps.error;
    }

    const configPropsBeansFeed = configProps.response!.beans;

    const effectiveConfigProps = search ? filterConfigPropsBeans(configPropsBeansFeed, search) : configPropsBeansFeed;

    const totalPropertiesCount = getPropertiesCount<IConfigPropsBean>(configPropsBeansFeed);
    const filteredPropertiesCount = getPropertiesCount<IConfigPropsBean>(effectiveConfigProps);

    const addonAfter = `${filteredPropertiesCount} / ${totalPropertiesCount}`;

    return (
        <>
            <PageSearch addonAfter={addonAfter} setSearch={setSearch} />
            <ConfigPropsTables effectiveConfigProps={effectiveConfigProps} loading={configProps.loading} />
        </>
    );
};

export default ConfigProps;
