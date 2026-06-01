import styles from "./styles.module.css";

export const CapabilitiesTopSection = () => {
    return (
        <div className={styles.MainWrapper}>
            <div>
                <span className={styles.Eyebrow}>Capabilities</span>
                <h2 className={styles.Title}>
                    Verbs for the JVM — same surface for <span className={styles.GreenText}>people</span> and{" "}
                    <span className={styles.BlueText}>agents</span>.
                </h2>
            </div>
            <p className={styles.IntroText}>
                Every capability is exposed twice. <em className={styles.AccentedText}>Engineers</em> reach it through a
                web console; <em className={styles.AccentedText}>AI agents</em> reach the same actions through an
                embedded MCP server. A single role model gates both — each identity, human or agent, sees only the data
                and actions its role permits.
            </p>
        </div>
    );
};
