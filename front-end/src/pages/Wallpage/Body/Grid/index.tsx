import { useEffect, useState } from "react";
import { getAllInstances } from "../../../../apis/AxileMasterApi";
import type { ApplicationInstance } from "../../../../types/ApplicationInstance";
import { Instance } from "./Instance";
import styles from "./styles.module.css";
import type { GridFilters } from "./GridFilters";

export const Grid = ({ filter }: { filter: GridFilters }) => {
  // TODO: load instances

  const [grid, setGrid] = useState<ApplicationInstance[] | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    getAllInstances()
      .then((data) => {
        setGrid(filterData(data));
      })
      .catch((e) => {
        // TODO: Is this correct? How do we get the error message of what went wrong
        setErrorMessage(e.message);
      });
  }, [filter]);

  if (errorMessage != null) {
    // Render the Error window
    return <></>;
  } else {
    // Render the Grid
    return (
      <div className={styles.ContentWrapper}>
        {grid?.map((element, index) => (
          <Instance key={index} instance={element}></Instance>
        ))}
      </div>
    );
  }

  function filterData(data: ApplicationInstance[]) {
    return data.filter((item) => {
      return (
        filter.queryString === undefined ||
        item.name.toLowerCase().includes(filter.queryString.toLocaleLowerCase())
      );
    });
  }
};
