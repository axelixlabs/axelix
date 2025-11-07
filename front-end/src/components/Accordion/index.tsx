import { type PropsWithChildren, type ReactNode, useState } from "react";

import styles from "./styles.module.css";

import ArrowIcon from "assets/icons/arrow.svg";

interface IProps {
    /**
     * Header of the accordion
     */
    header: ReactNode;

    /**
     * CSS styles for the accordion header
     */
    headerStyles?: string;

    /**
     * CSS classes for the accordion content.
     */
    contentStyles?: string;

    /**
     * Indicates whether the accordion is expanded
     */
    accordionExpanded?: boolean;
}

export const Accordion = ({
    header,
    children,
    headerStyles,
    contentStyles,
    accordionExpanded = false,
}: PropsWithChildren<IProps>) => {
    const [open, setOpen] = useState<boolean>(accordionExpanded);

    const handlerClick = (): void => {
        setOpen(!open);
    };

    return (
        <div className={`${styles.MainWrapper} ${open ? styles.Open : ""}`}>
            <div className={`${styles.HeaderWrapper} ${headerStyles}`} onClick={handlerClick}>
                <img src={ArrowIcon} alt="Arrow icon" className={styles.Icon} />
                <div className={styles.Header}>{header}</div>
            </div>
            <div className={styles.ContentWrapper}>
                <div className={`${styles.Content} ${contentStyles}`}>{children}</div>
            </div>
        </div>
    );
};
