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
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import { Copy, EmptyHandler, Loader } from "components";
import { fetchData, isCopyableField, resolveLangIcon, resolveOsIcon } from "helpers";
import { type IDetailsCardRecord, type IDetailsResponseBody, StatefulRequest } from "models";
import type { DetailsBuildValuesData } from "models/types/details";
import { getDetailsData } from "services";
import { DETAILS_I18N_PREFIX, VALUE_TRANSFORMERS } from "utils";

import { DetailsCard } from "./DetailsCard";
import { DetailsHeader } from "./DetailsFirstSection";
import styles from "./styles.module.css";

import BuildIcon from "assets/icons/build.svg";
import GitIcon from "assets/icons/git.svg";
import SpringIcon from "assets/icons/spring.svg";

const Details = () => {
    const { instanceId } = useParams();

    const [dataState, setDataState] = useState(StatefulRequest.loading<IDetailsResponseBody>());

    useEffect(() => {
        fetchData(setDataState, () => getDetailsData(instanceId!));
    }, []);

    if (dataState.loading) {
        return <Loader />;
    }

    if (dataState.error) {
        return <EmptyHandler isEmpty />;
    }

    const buildValues = (data: DetailsBuildValuesData): IDetailsCardRecord[] => {
        return Object.entries(data)
            .filter(([, value]) => value !== undefined)
            .map(([key, value]) => {
                return {
                    key: key,
                    value: (
                        <>
                            {VALUE_TRANSFORMERS[key] ? VALUE_TRANSFORMERS[key](value) : value}
                            {isCopyableField(key as string) && <Copy text={value} />}
                        </>
                    ),
                };
            });
    };

    const { serviceName, git, build, spring, runtime, os } = dataState.response!;

    return (
        <>
            <DetailsHeader instanceName={serviceName} />

            <div className={styles.InnerWrapper}>
                <div className={styles.ColumnWrapper}>
                    <DetailsCard
                        icon={GitIcon}
                        i18nPropertiesPrefix={`${DETAILS_I18N_PREFIX}.git`}
                        title="git"
                        records={buildValues(git)}
                    />
                    <DetailsCard
                        icon={BuildIcon}
                        i18nPropertiesPrefix={`${DETAILS_I18N_PREFIX}.build`}
                        title="build"
                        records={buildValues(build)}
                    />
                </div>
                <div className={styles.ColumnWrapper}>
                    <DetailsCard
                        icon={SpringIcon}
                        i18nPropertiesPrefix={`${DETAILS_I18N_PREFIX}.spring`}
                        title="spring"
                        records={buildValues(spring)}
                    />
                    <DetailsCard
                        icon={resolveLangIcon(runtime)}
                        i18nPropertiesPrefix={`${DETAILS_I18N_PREFIX}.runtime`}
                        title="runtime"
                        records={buildValues(runtime)}
                    />
                    <DetailsCard
                        icon={resolveOsIcon(os.name)}
                        i18nPropertiesPrefix={`${DETAILS_I18N_PREFIX}.os`}
                        title="os"
                        records={buildValues(os)}
                    />
                </div>
            </div>
        </>
    );
};

export default Details;
