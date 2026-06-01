import { HeroActions } from "./HeroActions";
import { HeroBackground } from "./HeroBackground";
import { HeroBenefits } from "./HeroBenefits";
import { HeroHeadline } from "./HeroHeadline";
import styles from "./styles.module.css";

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
};
