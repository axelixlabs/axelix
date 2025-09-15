import type { Dispatch, SetStateAction } from "react";
import type { GridFilters } from "./Body/Grid/GridFilters";

export interface GridFiltersProprs {
  /**
   * The reading accessor for the filter
   */
  filter: GridFilters;

  /**
   * The writing accessor fir the filter
   */
  filterSetter: Dispatch<SetStateAction<GridFilters>>;
}
