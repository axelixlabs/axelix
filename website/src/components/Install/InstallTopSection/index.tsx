import styles from "./styles.module.css";

export const InstallTopSection = () => {
    return (
        <div className={styles.MainWrapper}>
            <div>
                <span className={styles.Eyebrow}>Install</span>
                <h2 className={styles.Title}>
                    Three commands. <span className={styles.UnderlinedText}>No agent, no JVM flags, no redeploys.</span>
                </h2>
            </div>
            <p className={styles.IntroText}>
                Pick how you run things — Docker, Compose, or Kubernetes. Spin up the master, drop the starter into your
                app, point it at the master. The service shows up in the console{" "}
                <em className={styles.AccentedText}>and</em> on the MCP server within seconds.
            </p>
        </div>
    );
};
