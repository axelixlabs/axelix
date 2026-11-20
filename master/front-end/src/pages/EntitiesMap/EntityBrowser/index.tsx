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

import type { IMappedEntity } from "@/models";

import { EntityDetail } from "../EntityDetail";
import { EntityList } from "../EntityList";

import { EntityToolbar } from "./EntityToolbar";
import styles from "./styles.module.css";
import { useEntityBrowser } from "./useEntityBrowser";

interface IProps {
    /** All mapped entities reported for the instance. */
    entities: IMappedEntity[];
}

export const EntityBrowser = ({ entities }: IProps) => {
    const { t } = useTranslation();
    const browser = useEntityBrowser(entities);

    return (
        <div className={styles.MainWrapper}>
            <header>
                <h1 className="TextLarge">{t("Sider.entitiesMap")}</h1>
                <p className={styles.Description}>{t("EntitiesMap.description")}</p>
            </header>

            <EntityToolbar
                checkIds={browser.checkIds}
                selectedChecks={browser.selectedChecks}
                onChecksChange={browser.setSelectedChecks}
                filtered={browser.filtered}
                findingCount={browser.findingCount}
                matchingEntities={browser.withFindings.length}
                visibleEntities={browser.withFindings.length + browser.withoutFindings.length}
                totalEntities={entities.length}
            />

            <div className={styles.Split}>
                <EntityList
                    search={browser.search}
                    onSearchChange={browser.setSearch}
                    withFindings={browser.withFindings}
                    withoutFindings={browser.withoutFindings}
                    selectedEntity={browser.selected?.entity}
                    onSelect={browser.setSelectedName}
                />

                {browser.selected && <EntityDetail key={browser.selected.entity.name} result={browser.selected} />}
            </div>
        </div>
    );
};
