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
                        <EnvironmentModifiableTable
                            headerName={name}
                            properties={properties.map((property) => ({
                                key: property.name,
                                displayKey: property.name,
                                displayValue: property.value,
                                isPrimary: property.isPrimary,
                                configPropsBeanName: property.configPropsBeanName,
                            }))}
                            key={name}
                        />
                    ))}
                </>
            </EmptyHandler>
        </>
    );
};
