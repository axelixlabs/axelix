import { NavigationBarButton } from "./NavigationBarButton";

import styles from "./styles.module.css";

export const NavigationBar = () => {
  return (
    <div className={styles.NavigationBar}>
      <NavigationBarButton text="About" />
      <NavigationBarButton text="Profile" />
    </div>
  );
};
