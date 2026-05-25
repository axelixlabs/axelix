import styles from "./styles.module.css"

export const EnterpriseTopSection = () => {
    return (
        <div className={styles.MainWrapper}>
            <div>
                <span className={styles.Eyebrow}>Enterprise · early access</span>
                <h2 className={styles.Title}>
                    Open core stays open.{" "}
                    <span className={styles.UnderlinedText}>Enterprise lives on top.</span>
                </h2>
            </div>
            <p className={styles.Lead}>
                <em className={styles.AccentedText}>Axelix Enterprise</em> extends the open core with the controls,
                integrations and support platform, security and compliance teams
                ask for — without forking the surface engineers already learned.
                The OSS keeps shipping, in the open. Enterprise is what you reach
                for when one cluster becomes ten and audit asks who changed what at
                3 AM.
            </p>
        </div>
    )
}