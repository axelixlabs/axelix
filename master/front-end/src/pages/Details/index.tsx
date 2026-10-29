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
import { type ReactNode, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";

import { BranchIcon, ChipIcon, CommitIcon, LeafIcon, PackageIcon, TerminalIcon } from "@/assets";
import { Copy, EmptyHandler, Loader } from "@/components";
import { fetchData } from "@/helpers";
import { type IDetailsCardRecord, type IDetailsResponseBody, StatefulRequest } from "@/models";
import { getDetailsData } from "@/services";
import { VALUE_TRANSFORMERS } from "@/utils";

import { DetailsCard } from "./DetailsCard";
import { DetailsHeader } from "./DetailsFirstSection";
import styles from "./styles.module.css";

/**
 * Builds the card records, skipping the optional fields the instance did not report.
 */
const records = (values: Record<string, ReactNode | undefined>): IDetailsCardRecord[] => {
    return Object.entries(values)
        .filter(([, value]) => value !== undefined)
        .map(([key, value]) => ({ key, value: <>{value}</> }));
};

const code = (value: string | number | undefined, className: string = styles.Code) => {
    return value === undefined ? undefined : <span className={className}>{value}</span>;
};

const copyable = (value: ReactNode, text: string) => {
    return (
        <>
            {value}
            {text && <Copy text={text} />}
        </>
    );
};

const Details = () => {
    const { instanceId } = useParams();
    const { t } = useTranslation();

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

    const { serviceName, git, build, spring, runtime, os } = dataState.response!;

    const gitRecords = records({
        commitShaShort: copyable(git.commitShaShort && code(git.commitShaShort, styles.Chip), git.commitShaShort),
        branch: copyable(
            git.branch && (
                <span className={styles.BranchChip}>
                    <BranchIcon />
                    {git.branch}
                </span>
            ),
            git.branch,
        ),
        authorName: git.authorName && (
            <>
                <span className={styles.Avatar}>{git.authorName[0].toUpperCase()}</span>
                <span className={styles.Text}>{git.authorName}</span>
            </>
        ),
        authorEmail: code(git.authorEmail, styles.CodeMuted),
        commitTimestamp: code(VALUE_TRANSFORMERS.commitTimestamp(git.commitTimestamp)),
    });

    const osRecords = records({
        name: <span className={styles.Text}>{os.name}</span>,
        version: code(os.version, styles.CodeMuted),
        arch: code(os.arch, styles.CodeMuted),
        pid: code(os.pid, styles.CodeStrong),
        processUser: code(os.processUser, styles.CodeMuted),
    });

    const springRecords = records({
        springBootVersion: code(spring.springBootVersion, styles.CodeStrong),
        springFrameworkVersion: code(spring.springFrameworkVersion, styles.CodeStrong),
        springCloudVersion: code(spring.springCloudVersion, styles.CodeStrong),
    });

    const runtimeRecords = records({
        javaVersion: code(runtime.javaVersion, styles.CodeStrong),
        kotlinVersion: code(runtime.kotlinVersion, styles.CodeStrong),
        jdkVendor: <span className={styles.Text}>{runtime.jdkVendor}</span>,
        garbageCollector: code(
            t(`Details.garbageCollectors.${runtime.garbageCollector}`, { defaultValue: runtime.garbageCollector }),
            styles.Chip,
        ),
    });

    const buildRecords = records({
        artifact: copyable(code(build.artifact, styles.CodeStrong), build.artifact),
        version: code(build.version, styles.CodeStrong),
        group: code(build.group, styles.CodeMuted),
        time: code(build.time),
    });

    return (
        <>
            <DetailsHeader instanceName={serviceName} groupId={build.group} />

            <div className={styles.InnerWrapper}>
                <div className={styles.ColumnWrapper}>
                    <DetailsCard i18nPropertiesPrefix="Details.git" title="git" records={gitRecords}>
                        <CommitIcon />
                    </DetailsCard>
                    <DetailsCard i18nPropertiesPrefix="Details.os" title="os" records={osRecords}>
                        <TerminalIcon />
                    </DetailsCard>
                </div>
                <div className={styles.ColumnWrapper}>
                    <DetailsCard i18nPropertiesPrefix="Details.spring" title="spring" records={springRecords}>
                        <LeafIcon />
                    </DetailsCard>
                    <DetailsCard i18nPropertiesPrefix="Details.runtime" title="runtime" records={runtimeRecords}>
                        <ChipIcon />
                    </DetailsCard>
                    <DetailsCard i18nPropertiesPrefix="Details.build" title="build" records={buildRecords}>
                        <PackageIcon />
                    </DetailsCard>
                </div>
            </div>
        </>
    );
};

export default Details;
