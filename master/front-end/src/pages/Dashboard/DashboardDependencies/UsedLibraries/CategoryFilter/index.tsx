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
import type { EDependencyEcosystem } from "@/models";
import { DEPENDENCY_ECOSYSTEM_ORDER, dependencyEcosystemLabelKey } from "@/utils";

interface IProps {
    activeEcosystems: EDependencyEcosystem[];
    onChange: (ecosystems: EDependencyEcosystem[]) => void;
}

export const CategoryFilter = ({ activeEcosystems, onChange }: IProps) => {
    const { t } = useTranslation();

    return (
        <MultiSelectFilter
            label={t("Dashboard.Dependencies.columns.category")}
            allLabel={t("Dashboard.Dependencies.categoryPlaceholder")}
            options={DEPENDENCY_ECOSYSTEM_ORDER.map((value) => ({
                value,
                label: t(dependencyEcosystemLabelKey[value]),
            }))}
            selected={activeEcosystems}
            onChange={onChange}
        />
    );
};
