import styles from "./styles.module.css"

export const HeroHeadline = () => {
    return (
        <>
            <h1 className={styles.Title}>
                Control your <span className={styles.GreenText}>Spring&nbsp;Boot</span>{" "}
                in production — <span className={styles.UnderlinedText}>not the other way around.</span>
            </h1>

            <p className={styles.Lede}>
                Axelix is the open-source console for{" "}
                <em className={styles.AccentedText}>
                    debugging, observing and operating mission-critical Spring Boot
                    microservices.
                </em>{" "}
                Every capability is exposed twice — to engineers through the web,
                and to AI agents through an embedded MCP server.
            </p>
        </>
    )
}