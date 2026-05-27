import styles from "../shared.module.css"

interface IProps {
    refEl: any
}

export const YamlSnippet = ({ refEl }: IProps) => {
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
                    <span className={styles.St}>HMAC256</span>
                </span>
                <span className={styles.Line}>
                    {"        "}
                    <span className={styles.At}>signing-key</span>:{" "}
                    <span className={styles.St}>8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn</span>
                </span>
                <span className={styles.Line}>
                    {"    "}<span className={styles.At}>discovery</span>:
                </span>
                <span className={styles.Line}>
                    {"      "}
                    <span className={styles.At}>auto</span>:{" "}
                    <span className={styles.St}>true</span>
                </span>
                <span className={styles.Line}>
                    {"      "}
                    <span className={styles.At}>instance-name</span>:{" "}
                    <span className={styles.St}>my-app</span>
                </span>
                <span className={styles.Line}>
                    {"      "}
                    <span className={styles.At}>instance-actuator-url</span>:{" "}
                    <span className={styles.St}>https://my-app.com/actuator</span>
                </span>
                <span className={styles.Line}>
                    {"      "}
                    <span className={styles.At}>master-url</span>:{" "}
                    <span className={styles.St}>https://axelix-master.com/api/internal/service/register</span>
                </span>
            </code>
        </pre>
    );
} 