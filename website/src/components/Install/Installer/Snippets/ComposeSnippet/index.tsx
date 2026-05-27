import styles from "../shared.module.css"

interface IProps {
    refEl: any
}

export const ComposeSnippet = ({ refEl }: IProps) => {
    return (
        <pre
            className={styles.Snippet}
            ref={(el) => {
                refEl.current = el;
            }}
        >
            <code>
                <span className={styles.Line}><span className={styles.At}>services</span>:</span>
                <span className={styles.Line}>
                    {"  "}<span className={styles.At}>axelix</span>:
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.At}>image</span>:{" "}
                    <span className={styles.St}>ghcr.io/axelixlabs/axelix:v1.0.0-m1</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.At}>container_name</span>:{" "}
                    <span className={styles.St}>axelix</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.At}>ports</span>:
                </span>
                <span className={styles.Line}>
                    {"      "}- <span className={styles.St}>&quot;9444:8080&quot;</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.At}>environment</span>:
                </span>
                <span className={styles.Line}>
                    {"      "}
                    <span className={styles.Co}># Important: change for production use</span>
                </span>
                <span className={styles.Line}>
                    {"      "}
                    <span className={styles.At}>AXELIX_MASTER_AUTH_JWT_ALGORITHM</span>:{" "}
                    <span className={styles.St}>HMAC256</span>
                </span>
                <span className={styles.Line}>
                    {"      "}
                    <span className={styles.At}>AXELIX_MASTER_AUTH_JWT_SIGNING_KEY</span>:{" "}
                    <span className={styles.Kw}>8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.At}>restart</span>:{" "}
                    <span className={styles.St}>unless-stopped</span>
                </span>

                <span className={styles.Line}>
                    {"    "}
                </span>

                <span className={styles.Line}>
                    {"  "}<span className={styles.At}>your-spring-boot-app</span>:
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.At}>build</span>:{" "}
                </span>
                <span className={styles.Line}>
                    {"      "}
                    <span className={styles.At}>dockerfile</span>:{" "}
                    <span className={styles.St}>/path/to/Dockerfile</span>
                </span>
                <span className={styles.Line}>
                    {"      "}
                    <span className={styles.At}>context</span>:{" "}
                    <span className={styles.St}>.</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.At}>environment</span>:{" "}
                </span>
                <span className={styles.Line}>
                    {"      "}
                    <span className={styles.At}>AXELIX_SBS_DISCOVERY_AUTO</span>:{" "}
                    <span className={styles.St}>true</span>
                </span>
                <span className={styles.Line}>
                    {"      "}
                    <span className={styles.At}>AXELIX_SBS_DISCOVERY_INSTANCE_NAME</span>:{" "}
                    <span className={styles.St}>my-app</span>
                </span>
                <span className={styles.Line}>
                    {"      "}
                    <span className={styles.At}>AXELIX_SBS_DISCOVERY_INSTANCE_ACTUATOR_URL</span>:{" "}
                    <span className={styles.St}>http://my-app.com/actuator</span>
                </span>
                <span className={styles.Line}>
                    {"      "}
                    <span className={styles.At}>AXELIX_SBS_DISCOVERY_MASTER_URL</span>:{" "}
                    <span className={styles.St}>http://localhost:9444/api/internal/service/register</span>
                </span>

            </code>
        </pre>
    )
} 