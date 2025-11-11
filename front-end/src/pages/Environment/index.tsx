import { message } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader } from "components";
import { fetchData } from "helpers";
import { useAppSelector } from "hooks";
import { type IEnvironmentResponseBody, StatefulRequest } from "models";
import { getEnvironmentData } from "services";

import { EnvironmentProfiles } from "./EnvironmentProfiles";
import { EnvironmentTables } from "./EnvironmentTables";

export const Environment = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();

    const [environment, setEnvironment] = useState(StatefulRequest.loading<IEnvironmentResponseBody>());

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
        return <EmptyHandler isEmpty />;
    }

    const activeProfiles = environment.response!.activeProfiles;
    const propertySources = environment.response!.propertySources;

    return (
        <>
            <EnvironmentProfiles activeProfiles={activeProfiles} />
            <EnvironmentTables propertySources={propertySources} />
        </>
    );
};

export default Environment;
