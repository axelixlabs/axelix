import styles from "../shared.module.css"

interface IProps {
    refEl: any
}

export const PropertiesSnippet = ({ refEl }: IProps) => {
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
                    <span className={styles.St}>HMAC256</span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.At}>axelix.sbs.auth.jwt.signing-key</span>=
                    <span className={styles.St}>8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn</span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.At}>axelix.sbs.discovery.auto</span>=
                    <span className={styles.St}>true</span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.At}>axelix.sbs.discovery.instance-name</span>=
                    <span className={styles.St}>my-app</span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.At}>axelix.sbs.discovery.instance-actuator-url</span>=
                    <span className={styles.St}>https://my-app.com/actuator</span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.At}>axelix.sbs.discovery.master-url</span>=
                    <span className={styles.St}>https://axelix-master.com/api/internal/service/register</span>
                </span>
            </code>
        </pre>
    );
}