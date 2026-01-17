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
