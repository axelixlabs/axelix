import styles from "./styles.module.css";
import { InstallTopSection } from "./InstallTopSection";
import { Installer } from "./Installer";

export const Install = () => {
  return (
    <section className={styles.MainWrapper} id="install">
      <div className="wrap">
        <InstallTopSection />
        <Installer />
      </div>
    </section>
  );
}
