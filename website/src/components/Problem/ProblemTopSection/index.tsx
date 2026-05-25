import styles from "./styles.module.css"

export const ProblemTopSection = () => {
    return (
        <div className={styles.MainWrapper}>
            <span className={styles.Eyebrow}>An ordinary Tuesday</span>
            <h2 className={styles.Title}>
                At <em className={styles.AccentedText}>03:14 AM</em> your dashboard sees the fire.
                <br />
                <span className={styles.UnderlinedText}>You&apos;re the only one with a hose.</span>
            </h2>
        </div>
    )
}