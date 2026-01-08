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
import BuildIcon from "assets/icons/build.svg?react";
import GitIcon from "assets/icons/git.svg?react";
import SpringIcon from "assets/icons/spring.svg?react";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import { Copy, EmptyHandler, Loader } from "components";
import { fetchData, isCopyableField, resolveLangIcon, resolveOsIcon } from "helpers";
import { type IDetailsCardRecord, type IDetailsResponseBody, StatefulRequest } from "models";
import type { DetailsBuildValuesData } from "models/types/details";
import { getDetailsData } from "services";
import { VALUE_TRANSFORMERS } from "utils";

import { DetailsCard } from "./DetailsCard";
import { DetailsHeader } from "./DetailsFirstSection";
import styles from "./styles.module.css";

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

    const LangIcon = resolveLangIcon(runtime);
    const OsIcon = resolveOsIcon(os.name);

    return (
        <>
            <DetailsHeader instanceName={serviceName} />

            <div className={styles.InnerWrapper}>
                <div className={styles.ColumnWrapper}>
                    <DetailsCard i18nPropertiesPrefix="Details.git" title="git" records={buildValues(git)}>
                        <GitIcon className={styles.CardIcon} />
                    </DetailsCard>
                    <DetailsCard i18nPropertiesPrefix="Details.build" title="build" records={buildValues(build)}>
                        <BuildIcon className={styles.CardIcon} />
                    </DetailsCard>
                </div>
                <div className={styles.ColumnWrapper}>
                    <DetailsCard i18nPropertiesPrefix="Details.spring" title="spring" records={buildValues(spring)}>
                        <SpringIcon className={styles.CardIcon} color="#00ab55" />
                    </DetailsCard>
                    <DetailsCard i18nPropertiesPrefix="Details.runtime" title="runtime" records={buildValues(runtime)}>
                        <LangIcon className={styles.CardIcon} />
                    </DetailsCard>
                    <DetailsCard i18nPropertiesPrefix="Details.os" title="os" records={buildValues(os)}>
                        <OsIcon className={styles.CardIcon} />
                    </DetailsCard>
                </div>
            </div>
        </>
    );
};

export default Details;
