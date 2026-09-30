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
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";

import { EmptyHandler, Loader, PageSearch } from "@/components";
import { fetchData, filterDependencies, getDependenciesAutocompleteOptions } from "@/helpers";
import { type EDependencyEcosystem, type ESupportStatus, type IDependenciesAnalysis, StatefulRequest } from "@/models";
import { getDependenciesAnalysis } from "@/services";

import { DependenciesTable } from "./DependenciesTable";
import { EcosystemTabs } from "./EcosystemTabs";
import { FrameworkWindow } from "./FrameworkWindow";
import { SupportSignalFilter } from "./SupportSignalFilter";
import styles from "./styles.module.css";

const DependenciesAnalyzer = () => {
    const { instanceId } = useParams();

    const [analysis, setAnalysis] = useState(StatefulRequest.loading<IDependenciesAnalysis>());
    const [search, setSearch] = useState<string>("");
    const [ecosystem, setEcosystem] = useState<EDependencyEcosystem | null>(null);
    const [activeStatuses, setActiveStatuses] = useState<ESupportStatus[]>([]);

    const { t } = useTranslation();

    useEffect(() => {
        fetchData(setAnalysis, () => getDependenciesAnalysis(instanceId!));
    }, []);

    if (analysis.loading) {
        return <Loader />;
    }

    if (analysis.error) {
        return <EmptyHandler isEmpty />;
    }

    const { framework, dependencies, rootCoordinates, analyzedAt } = analysis.response!;

    if (dependencies.length === 0) {
        return <EmptyHandler isEmpty />;
    }

    const toggleStatus = (status: ESupportStatus): void => {
        setActiveStatuses((prev) => {
            if (prev.includes(status)) {
                return prev.filter((current) => current !== status);
            }

            return [...prev, status];
        });
    };

    const filtered = filterDependencies(dependencies, search, ecosystem, activeStatuses);
    const addonAfter = `${filtered.length} / ${dependencies.length}`;

    const autocompleteOptions = getDependenciesAutocompleteOptions(dependencies);

    return (
        <>
            <div className={styles.MainWrapper}>
                <FrameworkWindow framework={framework} analyzedAt={analyzedAt} />

                <div className={styles.Feed}>
                    <span className={`TextUltraSmall ${styles.Caption}`}>{t("DependenciesAnalyzer.feed.caption")}</span>
                    <div className={styles.Toolbar}>
                        <PageSearch
                            setSearch={setSearch}
                            addonAfter={addonAfter}
                            autocompleteOptions={autocompleteOptions}
                            removeBottomGutter
                        />

                        <EcosystemTabs
                            dependencies={dependencies}
                            search={search}
                            activeStatuses={activeStatuses}
                            activeEcosystem={ecosystem}
                            onPick={setEcosystem}
                        />

                        <SupportSignalFilter
                            dependencies={dependencies}
                            search={search}
                            activeEcosystem={ecosystem}
                            activeStatuses={activeStatuses}
                            onToggle={toggleStatus}
                            onClear={() => setActiveStatuses([])}
                        />
                    </div>

                    <DependenciesTable dependencies={filtered} rootCoordinates={rootCoordinates} />
                </div>
            </div>
        </>
    );
};

export default DependenciesAnalyzer;
