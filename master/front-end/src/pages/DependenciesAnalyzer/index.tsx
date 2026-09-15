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

import { EmptyHandler, Loader, PageSearch } from "@/components";
import { filterDependencies } from "@/helpers";
import { type EDependencyEcosystem, type ESupportSignal, type IDependenciesAnalysis, StatefulRequest } from "@/models";

import { DependenciesTable } from "./DependenciesTable";
import { EcosystemTabs } from "./EcosystemTabs";
import { PlatformWindow } from "./PlatformWindow";
import { SupportSignalFilter } from "./SupportSignalFilter";
import { DEPENDENCIES_ANALYSIS_MOCK } from "./mock";
import styles from "./styles.module.css";

const DependenciesAnalyzer = () => {
    const [analysis, setAnalysis] = useState(StatefulRequest.loading<IDependenciesAnalysis>());
    const [search, setSearch] = useState<string>("");
    const [ecosystem, setEcosystem] = useState<EDependencyEcosystem | null>(null);
    const [activeSignals, setActiveSignals] = useState<ESupportSignal[]>([]);

    const { t } = useTranslation();

    useEffect(() => {
        // TODO: Swap for fetchData(setAnalysis, () => getDependenciesAnalysis(instanceId!)) once the backend serves
        //  the dependency analysis, and drop ./mock.ts.
        setAnalysis(() => StatefulRequest.success(DEPENDENCIES_ANALYSIS_MOCK));
    }, []);

    if (analysis.loading) {
        return <Loader />;
    }

    if (analysis.error) {
        return <EmptyHandler isEmpty />;
    }

    const { platform, dependencies, rootCoordinates, analyzedAt } = analysis.response!;

    if (dependencies.length === 0) {
        return <EmptyHandler isEmpty />;
    }

    const toggleSignal = (signal: ESupportSignal): void => {
        setActiveSignals((prev) => {
            if (prev.includes(signal)) {
                return prev.filter((current) => current !== signal);
            }

            return [...prev, signal];
        });
    };

    const filtered = filterDependencies(dependencies, search, ecosystem, activeSignals);
    const addonAfter = `${filtered.length} / ${dependencies.length}`;

    return (
        <>
            <div className={styles.MainWrapper}>
                <PlatformWindow platform={platform} analyzedAt={analyzedAt} />

                <div className={styles.Feed}>
                    <span className={`TextUltraSmall ${styles.Caption}`}>{t("DependenciesAnalyzer.feed.caption")}</span>
                    <div className={styles.Toolbar}>
                        <PageSearch
                            setSearch={setSearch}
                            addonAfter={addonAfter}
                            removeBottomGutter
                            autocompleteOptions={dependencies.map((dependency) => {
                                return {
                                    value: dependency.coordinates,
                                    label: dependency.coordinates,
                                };
                            })}
                        />
                        <EcosystemTabs
                            dependencies={dependencies}
                            search={search}
                            activeSignals={activeSignals}
                            activeEcosystem={ecosystem}
                            onPick={setEcosystem}
                        />
                        <SupportSignalFilter
                            dependencies={dependencies}
                            search={search}
                            activeEcosystem={ecosystem}
                            activeSignals={activeSignals}
                            onToggle={toggleSignal}
                            onClear={() => setActiveSignals([])}
                        />
                    </div>
                    <DependenciesTable dependencies={filtered} rootCoordinates={rootCoordinates} />
                </div>
            </div>
        </>
    );
};

export default DependenciesAnalyzer;
