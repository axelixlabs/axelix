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

import { EmptyHandler } from "@/components";
import type { IMappedEntity } from "@/models";

import { EntityRow } from "./EntityRow";
import styles from "./styles.module.css";

interface IProps {
    /**
     * The entities to display, already filtered.
     */
    entities: IMappedEntity[];
}

export const EntitiesTable = ({ entities }: IProps) => {
    const { t } = useTranslation();

    if (entities.length === 0) {
        return <EmptyHandler isEmpty />;
    }

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.Table}>
                <div className={`TextUltraSmall ${styles.HeaderRow}`}>
                    <span>{t("EntitiesMap.columns.entity")}</span>
                    <span>{t("EntitiesMap.columns.associations")}</span>
                    <span>{t("EntitiesMap.columns.problems")}</span>
                </div>
                {entities.map((entity) => (
                    <EntityRow key={entity.name} entity={entity} />
                ))}
            </div>
        </div>
    );
};
