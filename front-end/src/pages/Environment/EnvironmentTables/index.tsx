import { EmptyHandler, PageSearch, TableSection } from "components";
import { useAppDispatch, useAppSelector } from "hooks";
import { filterProperties } from "store/slices";

export const EnvironmentTables = () => {
  const dispatch = useAppDispatch();

  const { propertySources, filteredPropertySources, environmentSearchText } = useAppSelector((store) => store.environment);

  const propertySourcesList = filteredPropertySources.length
    ? filteredPropertySources
    : propertySources;

  const noDataAfterSearch = !!environmentSearchText && !filteredPropertySources.length

  return (
    <>
      <PageSearch onChange={(value) => dispatch(filterProperties(value))} />

      <EmptyHandler isEmpty={noDataAfterSearch}>
        {propertySourcesList.map(({ name, properties }) => (
          <TableSection
            name={name}
            properties={properties}
            key={name}
          />
        ))}
      </EmptyHandler>
    </>
  );
};
