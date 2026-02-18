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
import { useParams } from "react-router-dom";

import { Copy, EmptyHandler, Loader } from "components";
import { fetchData, isCopyableField, resolveLangIcon, resolveOsIcon } from "helpers";
import {
    type DetailsBuildValuesData,
    type IDetailsCardRecord,
    type IDetailsResponseBody,
    StatefulRequest,
} from "models";
import { getDetailsData } from "services";
import { VALUE_TRANSFORMERS } from "utils";

import { DetailsCard } from "./DetailsCard";
import { DetailsHeader } from "./DetailsFirstSection";
import styles from "./styles.module.css";

import { BuildIcon, GitIcon, SpringIcon } from "assets";

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
