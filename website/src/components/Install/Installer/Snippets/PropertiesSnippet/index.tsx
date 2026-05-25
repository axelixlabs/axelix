import styles from "../shared.module.css"

interface IProps {
    refEl: any
}

export const PropertiesSnippet = ({ refEl }: IProps) => {
    return (
        <pre
            className={`${styles.Snippet} ${styles.Active}`}
            ref={(el) => {
                refEl.current = el;
            }}
        >
            <code>
                <span className={styles.Line}>
                    <span className={styles.At}>axelix.master.url</span>=
                    <span className={styles.St}>http://localhost:8080</span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.At}>axelix.auth.jwt.algorithm</span>=
                    <span className={styles.St}>HMAC256</span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.At}>axelix.auth.jwt.signing-key</span>=
                    <span className={styles.Kw}>{"${AXELIX_TOKEN}"}</span>
                </span>
            </code>
        </pre>
    );
}