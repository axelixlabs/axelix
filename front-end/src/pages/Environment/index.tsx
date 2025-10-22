import { message } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { Loader } from "components";
import { fetchData } from "helpers";
import { useAppSelector } from "hooks";
import { type IEnvironmentData, StatefulRequest } from "models";
import { getEnvironmentData } from "services";

import { EnvironmentProfiles } from "./EnvironmentProfiles";
import { EnvironmentTables } from "./EnvironmentTables";

export const Environment = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();

    const [environment, setEnvironment] = useState(StatefulRequest.loading<IEnvironmentData>());

    const updateProperty = useAppSelector((store) => store.updateProperty);

    const fetchEnvironment = (instanceId: string) => fetchData(setEnvironment, () => getEnvironmentData(instanceId));

    // todo So far, I haven't been able to find a way to combine the useEffects without causing an extra server request.
    useEffect(() => {
        if (instanceId) {
            fetchEnvironment(instanceId);
        }
    }, []);

    useEffect(() => {
        if (updateProperty.completedSuccessfully() && instanceId) {
            fetchEnvironment(instanceId);
            message.success(t("propertyChangedSuccessfully"));
        }
    }, [updateProperty]);

    if (environment.loading) {
        return <Loader />;
    }

    if (environment.error) {
        // todo change error handling in future
        return environment.error;
    }

    return (
        <>
            <EnvironmentProfiles activeProfiles={environment.response!.activeProfiles} />
            <EnvironmentTables propertySources={environment.response!.propertySources} />
        </>
    );
};

export default Environment;
