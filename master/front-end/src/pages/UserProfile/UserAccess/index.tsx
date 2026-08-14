import { AccessLogCard } from "./AccessLogCard";
import { AssignedRolesCard } from "./AssignedRolesCard";
import { EffectivePermissionsCard } from "./EffectivePermissionsCard";
import { SecurityCard } from "./SecurityCard";
import styles from "./styles.module.css";

export const UserAccess = () => {
    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.Column}>
                    <AssignedRolesCard />
                    <EffectivePermissionsCard />
                </div>

                <div className={styles.Column}>
                    <SecurityCard />
                    <AccessLogCard />
                </div>
            </div>
        </>
    );
};