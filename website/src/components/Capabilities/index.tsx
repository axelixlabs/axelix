import { CapabilitiesCompactCards } from "./CapabilitiesCompactCards";
import { CapabilitiesFeaturedCards } from "./CapabilitiesFeaturedCards";
import { CapabilitiesTopSection } from "./CapabilitiesTopSection";
import styles from "./styles.module.css";

export const Capabilities = () => {
    return (
        <section className={styles.MainWrapper} id="capabilities">
            <div className="wrap">
                <CapabilitiesTopSection />

                <CapabilitiesFeaturedCards />

                <CapabilitiesCompactCards />

                <div className={styles.CloserLinkWrapper}>
                    <a
                        href="https://axelix.io/docs/features/details"
                        target="_blank"
                        rel="noopener noreferrer"
                        className={styles.CloserLink}
                    >
                        More verbs <span className={styles.Arrow}>→</span> capability matrix
                    </a>
                </div>
            </div>
        </section>
    );
};
