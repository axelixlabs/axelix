import styles from "../shared.module.css"

interface IProps {
    refEl: any
}

export const K8sYamlSnippet = ({ refEl }: IProps) => {
    return (
        <pre
            className={styles.Snippet}
            ref={(el) => {
                refEl.current = el;
            }}
        >
            <code>
                <span className={styles.Line}>
                    <span className={styles.At}>axelix</span>:
                </span>
                <span className={styles.Line}>
                    {"  "}<span className={styles.At}>sbs</span>:
                </span>
                <span className={styles.Line}>
                    {"    "}<span className={styles.At}>auth</span>:
                </span>
                <span className={styles.Line}>
                    {"      "}<span className={styles.At}>jwt</span>:
                </span>
                <span className={styles.Line}>
                    {"        "}
                    <span className={styles.At}>algorithm</span>:{" "}
                    <span className={styles.St}>HMAC512</span>
                </span>
                <span className={styles.Line}>
                    {"        "}
                    <span className={styles.At}>signing-key</span>:{" "}
                    <span className={styles.St}>8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn</span>
                </span>
            </code>
        </pre>
    )
}