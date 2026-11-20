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
import { SearchOutlined } from "@ant-design/icons";

import { Input } from "antd";
import { useTranslation } from "react-i18next";

import type { IEntityResult } from "@/helpers";

import panelStyles from "../styles.module.css";

import { EntityGroup } from "./EntityGroup";
import { EntityListScroll } from "./EntityListScroll";
import styles from "./styles.module.css";

interface IProps {
    search: string;
    onSearchChange: (search: string) => void;
    withFindings: IEntityResult[];
    withoutFindings: IEntityResult[];
    selectedEntity: IEntityResult["entity"] | undefined;
    onSelect: (entity: string) => void;
}

export const EntityList = ({
    search,
    onSearchChange,
    withFindings,
    withoutFindings,
    selectedEntity,
    onSelect,
}: IProps) => {
    const { t } = useTranslation();

    return (
        <EntityListScroll>
            <div className={panelStyles.PanelHeader}>
                <h2 id="entities-map-list" className={styles.Title}>
                    {t("EntitiesMap.entities")}
                </h2>
                <Input
                    className={styles.Search}
                    prefix={<SearchOutlined />}
                    allowClear
                    value={search}
                    onChange={(event) => onSearchChange(event.target.value)}
                    aria-label={t("EntitiesMap.search")}
                    placeholder={t("EntitiesMap.search")}
                />
            </div>

            <EntityGroup
                label={t("EntitiesMap.withFindings")}
                results={withFindings}
                selectedEntity={selectedEntity}
                onSelect={onSelect}
            />
            <EntityGroup
                label={t("EntitiesMap.withoutFindings")}
                results={withoutFindings}
                selectedEntity={selectedEntity}
                onSelect={onSelect}
            />
            {withFindings.length === 0 && withoutFindings.length === 0 && (
                <p className={panelStyles.PanelHeader}>{t("EntitiesMap.noMatches")}</p>
            )}
        </EntityListScroll>
    );
};
