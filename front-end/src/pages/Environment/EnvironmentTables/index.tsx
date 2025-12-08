/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { useState } from "react";

import { EmptyHandler, PageSearch } from "components";
import { filterPropertySources, getPropertiesCount } from "helpers";
import type { IEnvironmentPropertySource } from "models";

import { EnvironmentModifiableTable } from "../EnvironmentModifiableTable";

interface IProps {
    /**
     * The list of property sources to render
     */
    propertySources: IEnvironmentPropertySource[];
}

/**
 * Applies deduplication in case the property name is present in multiple property sources with the same name
 */
function buildAutoCompleteOptions(propertySources: IEnvironmentPropertySource[]) {
    return [...new Set(propertySources.flatMap(({ properties }) => properties).map((p) => p.name))].map((value) => {
        return {
            value: value,
        };
    });
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
                    {effectivePropertySources.map(({ name, properties }) => (
                        <EnvironmentModifiableTable headerName={name} properties={properties} key={name} />
                    ))}
                </>
            </EmptyHandler>
        </>
    );
};
