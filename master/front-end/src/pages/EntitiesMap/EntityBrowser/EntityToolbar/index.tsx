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
import { useTranslation } from "react-i18next";

import { MultiSelectFilter } from "@/components";
import { ENTITY_CHECK_LABELS } from "@/helpers";

import styles from "./styles.module.css";

interface IProps {
    checkIds: string[];
    selectedChecks: string[];
    onChecksChange: (checks: string[]) => void;
    filtered: boolean;
    findingCount: number;
    matchingEntities: number;
    visibleEntities: number;
    totalEntities: number;
}

export const EntityToolbar = ({
    checkIds,
    selectedChecks,
    onChecksChange,
    filtered,
    findingCount,
    matchingEntities,
    visibleEntities,
    totalEntities,
}: IProps) => {
    const { t } = useTranslation();
    const findings = t("EntitiesMap.findingCount", { count: findingCount });

    return (
        <div className={styles.Toolbar}>
            <MultiSelectFilter
                label={t("EntitiesMap.checkFilter")}
                allLabel={t("EntitiesMap.allChecks")}
                options={checkIds.map((value) => {
                    const label = ENTITY_CHECK_LABELS.get(value);
                    return { value, label: label ? t(`EntitiesMap.problems.${label}`) : value };
                })}
                selected={selectedChecks}
                onChange={onChecksChange}
                searchable
            />
            <p className={styles.Summary} role="status">
                {filtered
                    ? t("EntitiesMap.filteredSummary", {
                          findings,
                          entities: t("EntitiesMap.entityCount", { count: visibleEntities }),
                      })
                    : t("EntitiesMap.summary", {
                          findings,
                          matching: matchingEntities,
                          total: totalEntities,
                          count: totalEntities,
                      })}
            </p>
        </div>
    );
};
