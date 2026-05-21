import styles from "./styles.module.css"

export const IncidentCardHeader = () => {
    return (
        <div className={styles.MainWrapper}>
            <span className={styles.Lhs}>
                <span className={styles.Dot}></span> incident  orders-api
            </span>
            <span className={styles.Badge}>P1  customer-facing</span>
            <span className={styles.Date}>tue 02:14 03:38</span>
        </div>
    )
} 