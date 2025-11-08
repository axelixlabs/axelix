import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import { Copy, Loader } from "components";
import { fetchData, isCopyableField, resolveLangIcon, resolveOsIcon } from "helpers";
import { type IDetailsCardRecord, type IDetailsResponseBody, StatefulRequest } from "models";
import type { DetailsBuildValuesData } from "models/types/details";
import { getDetailsData } from "services";
import { DETAILS_I18N_PREFIX } from "utils";

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
        // todo change error handling in future
        return dataState.error;
    }

    const buildValues = (data: DetailsBuildValuesData): IDetailsCardRecord[] => {
        return Object.entries(data).map(([key, value]) => {
            return {
                key: key,
                value: (
                    <>
                        {value as string}
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
