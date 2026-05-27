import styles from "../shared.module.css"

interface IProps {
    refEl: any
}

export const DockerSnippet = ({ refEl }: IProps) => {
    return (
        <pre
            className={styles.Snippet}
            ref={(el) => {
                refEl.current = el;
            }}
        >
            <code>
                <span className={styles.Line}>
                    <span className={styles.Co}># Run the docker image (optionally pulls an image)</span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.Cm}>docker run</span>{" "}
                    <span className={styles.Nl}>\</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.Ar}>--publish</span>{" "}
                    <span className={styles.St}>8080:8080</span>{" "}
                    <span className={styles.Nl}>\</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.Ar}>-e</span> AXELIX_MASTER_AUTH_JWT_ALGORITHM=
                    <span className={styles.St}>HMAC256</span>{" "}
                    <span className={styles.Nl}>\</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.Ar}>-e</span> AXELIX_MASTER_AUTH_JWT_SIGNING_KEY=
                    <span className={styles.St}>8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn</span>{" "}
                    <span className={styles.Nl}>\</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.Ar}>--name</span>{" "}
                    <span className={styles.St}>axelix</span>{" "}
                    <span className={styles.Nl}>\</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.Ar}>--detach</span>{" "}
                    <span className={styles.Nl}>\</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.St}>ghcr.io/axelixlabs/axelix:v1.0.0-M1</span>
                </span>
            </code>
        </pre>
    )
} 