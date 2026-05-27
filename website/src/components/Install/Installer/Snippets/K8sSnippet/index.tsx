import styles from "../shared.module.css"

interface IProps {
    refEl: any
}

export const K8sSnippet = ({ refEl }: IProps) => {
    return (
        <pre
            className={styles.Snippet}
            ref={(el) => {
                refEl.current = el;
            }}
        >
            <code>
                <span className={styles.Line}>
                    <span className={styles.Co}># Install Axelix Master via Helm</span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.Co}># Important: Please, change the algorithm and the key for production use</span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.Cm}>helm repo add</span>{" "}
                    <span className={styles.St}>axelixlabs</span>{" "}
                    <span className={styles.St}>https://axelixlabs.github.io/helm-charts</span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.Cm}>helm repo update</span>{" "}
                </span>
                <span className={styles.Line}>
                    <span className={styles.Cm}>helm install</span>{" "}
                    <span className={styles.St}>axelix</span>{" "}
                    <span className={styles.St}>axelixlabs/axelix</span>{" "}
                    <span className={styles.Nl}>\</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.Ar}>--set</span>{" "}
                    <span className={styles.St}>axelix.master.auth.jwt.algorithm=HMAC256</span>{" "}
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.Ar}>--set</span>{" "}
                    <span className={styles.St}>axelix.master.auth.jwt.signingKey=8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn</span>{" "}
                </span>
            </code>
        </pre>
    )
} 