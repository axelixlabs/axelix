import type { ILicensing } from "models"
import styles from "./styles.module.css"
import dayjs from "dayjs"

interface IProps {
    licensing: ILicensing
}

export const LicenseKeyInfo = ({ licensing }: IProps) => {
    const { licenseId, issuedTo, validUntil } = licensing

    const validTo = validUntil && dayjs(validUntil).format("YYYY-MM-DD")

    return (
        <>
            <div className={styles.ActiveLicenseKeyInfoWrapper}>
                <div className={styles.ActiveLicenseKeyInfoLabel}>Currently active</div>
                <div className={styles.ActiveLicenseKeyInfoValue}>{licenseId || "-"}</div>
                <div className={styles.ActiveLicenseKeyInfoOrg}>{issuedTo || "-"}</div>
                <div className={styles.ActiveLicenseKeyInfoValue}>{validTo ? `until ${validTo}` : "-"}</div>
            </div>
        </>
    )
}