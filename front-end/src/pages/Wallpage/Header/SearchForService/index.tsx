import type { ChangeEvent } from "react";
import type { GridFiltersProprs } from "../../GridFiltersProps";

import styles from "./styles.module.css";

export const SearchForService = ({
  gridFilterProps,
}: {
  gridFilterProps: GridFiltersProprs;
}) => {
  return (
    <div className={styles.TextField}>
      <label className={styles.SearchNavbarLabel} htmlFor="search">
        Search:
      </label>
      <input
        className={styles.SearchNavbar}
        type="text"
        name="search"
        id="search"
        placeholder="Search"
        onChange={(e: ChangeEvent<HTMLInputElement>) => {
          const oldFilter = gridFilterProps.filter;
          gridFilterProps.filterSetter(oldFilter.copy(e.target.value));
        }}
      />
    </div>
  );
};
