import { useState } from "react";

import { EmptyHandler, ModifiableTableSection, PageSearch } from "components";
import { filterPropertySources, getPropertiesCount } from "helpers";
import type { IEnvironmentPropertySource } from "models";

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

    return (
        <>
            <PageSearch addonAfter={addonAfter} setSearch={setSearch} />

            <EmptyHandler isEmpty={effectivePropertySources.length === 0}>
                <>
                    {effectivePropertySources.map(({ name, properties }) => (
                        <ModifiableTableSection
                            headerName={name}
                            properties={properties.map((property) => ({
                                key: property.name,
                                displayKey: property.name,
                                displayValue: property.value,
                                isPrimary: property.isPrimary,
                            }))}
                            key={name}
                        />
                    ))}
                </>
            </EmptyHandler>
        </>
    );
};
