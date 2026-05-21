import styles from "./styles.module.css";
import { HeroBackground } from "./HeroBackground";
import { HeroBenefits } from "./HeroBenefits";
import { HeroActions } from "./HeroActions";
import { HeroHeadline } from "./HeroHeadline";

export const Hero = () => {
  return (
    <header className={styles.MainWrapper}>
      <HeroBackground />

      <div className={`wrap ${styles.InnerWrapper}`}>
        <div className={styles.ContentWrapper}>
          <HeroHeadline />

          <HeroActions />

          <HeroBenefits />
        </div>
      </div>
    </header>
  );
}