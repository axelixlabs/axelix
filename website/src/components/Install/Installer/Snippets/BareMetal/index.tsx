import styles from "../shared.module.css"

interface IProps {
    refEl: any
}

export const BareMetal = ({ refEl }: IProps) => {
    return (
        <pre
            className={styles.Snippet}
            ref={(el) => {
                refEl.current = el;
            }}
        >
            <code>
                <span className={styles.Line}>
                    <span className={styles.Co}># Download and run the Axelix Master JAR</span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.Co}># Important: Please, change the algorithm and the key for production use</span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.Cm}>java -jar master.jar</span>{" "}
                    <span className={styles.Nl}>\</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.St}>--server.port=8080</span>{" "}
                    <span className={styles.Nl}>\</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.St}>--axelix.master.auth.jwt.algorithm=HMAC256</span>{" "}
                    <span className={styles.Nl}>\</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.St}>--axelix.master.auth.jwt.signing-key=8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn</span>
                </span>
            </code>
        </pre>
    );
} 