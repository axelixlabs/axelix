import styles from "./styles.module.css";

export const FaqTopSection = () => {
    return (
        <div className={styles.MainWrapper}>
            <div>
                <span className={styles.Eyebrow}>FAQ</span>
                <h2 className={styles.Title}>
                    Questions, <span className={styles.UnderlinedText}>answered.</span>
                </h2>
            </div>
            <p className={styles.Intro}>
                Everything you&apos;d ask before pointing answer console at your fleet — license, safety, platform
                support, and what&apos;s next.
            </p>
        </div>
    );
};
