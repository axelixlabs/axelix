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
import { useState } from "react";

import { EmptyHandler, PageSearch } from "components";
import { buildAutoCompleteOptions, filterPropertySources, getPropertiesCount } from "helpers";
import type { IEnvironmentPropertySource } from "models";

import { EnvironmentModifiableTable } from "../EnvironmentModifiableTable";

interface IProps {
    /**
     * The list of property sources to render
     */
    propertySources: IEnvironmentPropertySource[];
}

export const EnvironmentTables = ({ propertySources }: IProps) => {
    const [search, setSearch] = useState<string>("");
    const effectivePropertySources = search ? filterPropertySources(propertySources, search) : propertySources;

    const totalPropertiesCount = getPropertiesCount<IEnvironmentPropertySource>(propertySources);
    const filteredPropertiesCount = getPropertiesCount<IEnvironmentPropertySource>(effectivePropertySources);

    const addonAfter = `${filteredPropertiesCount} / ${totalPropertiesCount}`;

    const autocompleteOptions = buildAutoCompleteOptions(effectivePropertySources);

    return (
        <>
            <PageSearch addonAfter={addonAfter} setSearch={setSearch} autocompleteOptions={autocompleteOptions} />

            <EmptyHandler isEmpty={effectivePropertySources.length === 0}>
                <>
                    {effectivePropertySources.map((propertySource) => (
                        <EnvironmentModifiableTable propertySource={propertySource} key={propertySource.name} />
                    ))}
                </>
            </EmptyHandler>
        </>
    );
};
