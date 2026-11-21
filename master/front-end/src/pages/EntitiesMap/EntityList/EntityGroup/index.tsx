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

import type { IEntityResult } from "@/helpers";

import panelStyles from "../../styles.module.css";

import styles from "./styles.module.css";

interface IProps {
    label: string;
    results: IEntityResult[];
    selectedEntity: IEntityResult["entity"] | undefined;
    onSelect: (entity: string) => void;
}

export const EntityGroup = ({ label, results, selectedEntity, onSelect }: IProps) => {
    const { t } = useTranslation();

    return (
        <details className={panelStyles.Disclosure} hidden={results.length === 0} open>
            <summary className={`${styles.GroupLabel} ${panelStyles.DisclosureHeader}`}>
                {label} · {results.length}
            </summary>
            <div className={panelStyles.DisclosureContent}>
                <ul>
                    {results.map(({ entity, findings }) => (
                        <li key={entity.name}>
                            <button
                                type="button"
                                className={styles.Entity}
                                aria-pressed={selectedEntity === entity}
                                onClick={() => onSelect(entity.name)}
                            >
                                <span>
                                    <strong>{entity.name}</strong>
                                    <code className={styles.Table}>{entity.table}</code>
                                </span>
                                {findings.length > 0 && (
                                    <span
                                        className={styles.Count}
                                        aria-label={t("EntitiesMap.findingCount", { count: findings.length })}
                                    >
                                        {findings.length}
                                    </span>
                                )}
                            </button>
                        </li>
                    ))}
                </ul>
            </div>
        </details>
    );
};
