import { NavigationBar } from "./NavigationBar";
import { SearchForService } from "./SearchForService";
import type { GridFiltersProprs } from "../GridFiltersProps";

import styles from "./styles.module.css";

export const Header = ({
  gridFilterProps,
}: {
  gridFilterProps: GridFiltersProprs;
}) => {
  return (
    <header className={styles.Header}>
      <div className={styles.HeaderInner}>
        <span className={styles.MainTitle}>Axile Dashboard</span>
        <div className={styles.SearchBarHeader}>
          <SearchForService gridFilterProps={gridFilterProps} />
        </div>
        <div className={styles.NavBarHeader}>
          <NavigationBar />
        </div>
      </div>
    </header>
  );
};

{
  /* TODO: Change for the actual SVG icon of the project */
}
{
  /* <svg
                                className='bar_logo'
                                version="1.1"
                                baseProfile="full"
                                width="10%"
                                height="10%"
                                xmlns="http://www.w3.org/2000/svg">
                            <rect width="100%" height="50%" fill="black" />
                        </svg> */
}
