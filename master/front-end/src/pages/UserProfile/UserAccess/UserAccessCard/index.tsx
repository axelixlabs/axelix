import type { ReactNode } from "react";

import styles from "./styles.module.css";

interface IProps {
    title: ReactNode;
    action?: ReactNode;
    children: ReactNode;
}

export const UserAccessCard = ({ title, action, children }: IProps) => {
    return (
        <>
            <div className={styles.Card}>
                <div className={styles.CardHeader}>
                    <span className={styles.CardTitle}>{title}</span>
                    {action}
                </div>
                <div className={styles.CardBody}>{children}</div>
            </div>
        </>
    );
};