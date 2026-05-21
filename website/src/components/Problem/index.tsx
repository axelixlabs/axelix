import { IncidentCard } from "./IncidentCard";
import { PorblemRightCards } from "./ProblemRightCards";
import { ProblemTopSection } from "./ProblemTopSection";
import styles from "./styles.module.css";

export const Problem = () => {
    return (
        <section className={styles.MainWrapper} id="problem">
            <div className="wrap">
                <ProblemTopSection />

                <div className={styles.ContentWrapper}>
                    <IncidentCard />
                    <PorblemRightCards />
                </div>

                <p className={styles.SectionFooterText}>
                    Every other tool was built to <em className={styles.AccentedText}>watch</em> production. <b className={styles.BoldedText}>Axelix was built to operate it </b>
                    from a browser, or from an AI agent, with the same auth, the same audit trail.
                </p>
            </div>
        </section>
    );
};

