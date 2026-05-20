import styles from "./styles.module.css"

export const ProblemTopSection = () => {
    return (
        <div className={styles.MainWrapper}>
            <span className={styles.Eyebrow}>An ordinary Tuesday</span>
            <h2 className={styles.Title}>
                At <span className={styles.Accent}>03:14 AM</span> your dashboard sees the fire.
                <br />
                <span className={styles.Stroke}>You&apos;re the only one with a hose.</span>
            </h2>
        </div>
    )
}