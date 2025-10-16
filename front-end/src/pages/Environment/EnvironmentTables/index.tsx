import { useState } from "react";

import { EmptyHandler, PageSearch, ModifiableTableSection } from "components";
import type { IEnvironmentPropertySource } from "models";
import { filterPropertySources } from "helpers";
import { useAppSelector } from "hooks";

export const EnvironmentTables = () => {

  const { propertySources } = useAppSelector((store) => store.environment);

  const [isSearched, setIsSearched] = useState<boolean>(false)
  const [filteredPropertySources, setFilteredPropertySources] = useState<IEnvironmentPropertySource[]>([])

  const propertySourcesList = isSearched ? filteredPropertySources : propertySources;
  const noSearchResults = isSearched && !filteredPropertySources.length
  const addonAfter = `${isSearched ? filteredPropertySources.length : propertySources.length} / ${propertySources.length}`;

  const handleSearchChange = (search: string): void => {
    const isSearching = Boolean(search);
    setIsSearched(isSearching);

    if (!isSearching) {
      setFilteredPropertySources([]);
      return;
    }

    setFilteredPropertySources(filterPropertySources(propertySources, search))
  };

  return (
    <>
      <PageSearch addonAfter={addonAfter} onChange={handleSearchChange} />

      <EmptyHandler isEmpty={noSearchResults}>
        {propertySourcesList.map(({ name, properties }) => (
          <ModifiableTableSection
            headerName={name}
            properties={
              properties.map(
                (property) => (
                  {
                    key: property.key,
                    displayKey: property.key,
                    displayValue: property.value
                  }
                )
              )
            }
            key={name}
          />
        ))}
      </EmptyHandler>
    </>
  );
};
