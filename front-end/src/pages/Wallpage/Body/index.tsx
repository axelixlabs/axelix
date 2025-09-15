import { Grid } from "./Grid";
import type { GridFilters } from "./Grid/GridFilters";

import styles from "./styles.module.css";

export const Body = ({ filter }: { filter: GridFilters }) => {
  return (
    <div className={styles.MainWrapper}>
      <div className={styles.Sidebar}>E+HEEEYEY</div>
      <div className={styles.Content}>
        <Grid filter={filter}></Grid>
      </div>
    </div>
  );
};
