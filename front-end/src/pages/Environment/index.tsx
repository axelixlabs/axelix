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
import { App } from "antd";
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

const Environment = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const { message } = App.useApp();

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
