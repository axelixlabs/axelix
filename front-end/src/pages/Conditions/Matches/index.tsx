import type { PropsWithChildren } from "react";

import styles from "../styles.module.css";

interface IProps {
    /**
     * Match tab title
     */
    title: string;
}

export const Matches = ({ title, children }: PropsWithChildren<IProps>) => {
    return (
        <>
            <div className={`TextMedium ${styles.ConditionsMainTitle}`}>{title}</div>
            {children}
        </>
    );
};
