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
import { useParams } from "react-router";

import { EmptyHandler, Loader, PageSearch } from "@/components";
import { fetchData, filterEntities, isEntityProblematic } from "@/helpers";
import { type EAssociationProblem, type IPersistenceInsights, StatefulRequest } from "@/models";
import { getTransactionalData } from "@/services";

import { AssociationProblemFilter } from "./AssociationProblemFilter";
import { CleanState } from "./CleanState";
import { EntitiesMapSummary } from "./EntitiesMapSummary";
import { EntitiesTable } from "./EntitiesTable";
import styles from "./styles.module.css";

const EntitiesMap = () => {
    const [insights, setInsights] = useState(StatefulRequest.loading<IPersistenceInsights>());
    const [search, setSearch] = useState<string>("");
    const [activeProblemTypes, setActiveProblemTypes] = useState<EAssociationProblem[]>([]);

    const { instanceId } = useParams();

    useEffect(() => {
        fetchData(setInsights, () => getTransactionalData(instanceId!));
    }, []);

    if (insights.loading) {
        return <Loader />;
    }

    if (insights.error) {
        return <EmptyHandler isEmpty />;
    }

    const entities = insights.response!.entitiesMap?.entities ?? [];
    const analyzed = entities.length;

    if (analyzed === 0) {
        return <EmptyHandler isEmpty />;
    }

    const problematicEntities = entities.filter(isEntityProblematic);
    const problematic = problematicEntities.length;

    if (problematic === 0) {
        return <CleanState analyzed={analyzed} />;
    }

    const toggleProblemType = (type: EAssociationProblem): void => {
        setActiveProblemTypes((prev) => {
            if (prev.includes(type)) {
                return prev.filter((current) => current !== type);
            }

            return [...prev, type];
        });
    };

    const filtered = filterEntities(problematicEntities, search, activeProblemTypes);
    const addonAfter = `${filtered.length} / ${problematic}`;

    return (
        <>
            <div className={styles.MainWrapper}>
                <EntitiesMapSummary analyzed={analyzed} problematic={problematic} clean={analyzed - problematic} />
                <div className={styles.Toolbar}>
                    <PageSearch
                        setSearch={setSearch}
                        addonAfter={addonAfter}
                        removeBottomGutter
                        autocompleteOptions={problematicEntities.map((value) => {
                            return {
                                value: value.name,
                                label: value.name,
                            };
                        })}
                    />
                    <AssociationProblemFilter activeProblemTypes={activeProblemTypes} onToggle={toggleProblemType} />
                </div>
                <EntitiesTable entities={filtered} />
            </div>
        </>
    );
};

export default EntitiesMap;
