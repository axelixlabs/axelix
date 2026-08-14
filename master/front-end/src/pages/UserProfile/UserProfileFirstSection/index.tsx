import styles from "./styles.module.css"
import { Button } from "antd"

interface IProps {
    username: string
}

export const UserProfileFirstSection = ({ username }: IProps) => {
    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.HeaderInfo}>
                    <div className={`TextSmall ${styles.Avatar}`}>DR</div>
                    <div>
                        <div className={`TextMedium ${styles.Username}`}>{username}</div>
                        <div className={`TextSmall ${styles.Subtitle}`}>
                            Reliability Engineer · Operations · employee 8842 · SSO (Okta)
                        </div>
                    </div>
                </div>

                <div className={styles.ActionButtonsWrapper}>
                    <Button>Edit profile</Button>
                    <Button danger>Suspend</Button>
                </div>
            </div>
        </>
    )
}