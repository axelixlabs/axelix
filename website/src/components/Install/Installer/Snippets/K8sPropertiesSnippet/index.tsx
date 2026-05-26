import styles from "../shared.module.css"

interface IProps {
    refEl: any
}

export const K8sPropertiesSnippet = ({ refEl }: IProps) => {
    return (
        <pre
            className={styles.Snippet}
            ref={(el) => {
                refEl.current = el;
            }}
        >
            <code>
                <span className={styles.Line}>
                    <span className={styles.At}>axelix.sbs.auth.jwt.algorithm</span>=
                    <span className={styles.St}>HMAC512</span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.At}>axelix.sbs.auth.jwt.signing-key</span>=
                    <span className={styles.St}>8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn</span>
                </span>
            </code>
        </pre>
    )
}