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

export const EnvironmentTables = ({ propertySources }: IProps) => {
    const [search, setSearch] = useState<string>("");
    const effectivePropertySources = search ? filterPropertySources(propertySources, search) : propertySources;

    const totalPropertiesCount = getPropertiesCount<IEnvironmentPropertySource>(propertySources);
    const filteredPropertiesCount = getPropertiesCount<IEnvironmentPropertySource>(effectivePropertySources);

    const addonAfter = `${filteredPropertiesCount} / ${totalPropertiesCount}`;

    const autocompleteOptions = propertySources.flatMap(({ properties }) =>
        properties.map(({ name }) => ({ value: name })),
    );

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
