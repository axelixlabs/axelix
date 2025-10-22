import { useState } from "react";

import { EmptyHandler, PageSearch, ModifiableTableSection } from "components";
import type { IEnvironmentPropertySource } from "models";
import { filterPropertySources } from "helpers";

interface IProps {

  /**
   * The list of property sources to render
   */
  propertySources: IEnvironmentPropertySource[];
}

export const EnvironmentTables = ({ propertySources } : IProps) => {

  const [search, setSearch] = useState<string>("")

  const effectivePropertySources = search
    ? filterPropertySources(propertySources, search)
    : propertySources

  const addonAfter = `${effectivePropertySources.length} / ${propertySources.length}`;

  return (
    <>
      <PageSearch addonAfter={addonAfter} search={search} setSearch={setSearch} />

      <EmptyHandler isEmpty={effectivePropertySources.length === 0}>
        {effectivePropertySources.map(({ name, properties }) => (
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
