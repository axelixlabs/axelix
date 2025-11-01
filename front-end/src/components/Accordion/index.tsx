import { type PropsWithChildren, type ReactNode, useState } from "react";

import styles from "./styles.module.css";

import ArrowIcon from "assets/icons/arrow.svg";

interface IProps {
    /**
     * Header of the accordion
     */
    header: ReactNode;
    /**
     * True when the selected dependency equals the bean name
     */
    isActiveKey?: boolean;
}

export const Accordion = ({ header, children, isActiveKey }: PropsWithChildren<IProps>) => {
    const [open, setOpen] = useState<boolean>(false);

    const handlerClick = (): void => {
        setOpen(!open);
    };

    return (
        <div className={`${styles.MainWrapper} ${isActiveKey || open ? styles.Open : ""}`}>
            <div className={styles.HeaderWrapper} onClick={handlerClick}>
                <img src={ArrowIcon} alt="Arrow icon" className={styles.Icon} />
                <div className={styles.Header}>{header}</div>
            </div>
            <div className={styles.ContentWrapper}>
                <div className={styles.Content}>{children}</div>
            </div>
        </div>
    );
};
