import styles from "./styles.module.css";

export const HeroHeadline = () => {
    return (
        <>
            <h1 className={styles.Title}>
                Go <span className={styles.GreenText}>AI-Native</span> with{" "}
                <span className={styles.GreenText}>Spring&nbsp;Boot</span>: The Secret to{" "}
                <span className={styles.UnderlinedText}>Skyrocketing</span> Engineering Velocity
            </h1>

            <p className={styles.Lede}>
                Axelix is the <span className={styles.GreenTextBold}>AI-Native</span>, OSS solution for{" "}
                <em className={styles.AccentedText}>
                    debugging, observing and operating mission-critical Spring Boot microservices.
                </em>{" "}
                Every capability is exposed twice — to human engineers through the web, and to AI agents through an
                embedded MCP server.
            </p>
        </>
    );
};
