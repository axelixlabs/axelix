import { type PropsWithChildren, type ReactNode, useState } from "react";

import styles from "./styles.module.css";

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
}

export const ThreadDumpAccordion = ({ header, children, headerStyles, contentStyles }: PropsWithChildren<IProps>) => {
    const [open, setOpen] = useState<boolean>(false);

    const handlerClick = (): void => {
        setOpen(!open);
    };

    return (
        <div className={`${styles.MainWrapper} ${open ? styles.Open : ""}`}>
            <div className={`${styles.HeaderWrapper} ${headerStyles}`} onClick={handlerClick}>
                <div className={styles.Header}>{header}</div>
            </div>
            {open && (
                <div className={styles.ContentWrapper}>
                    <div className={`${styles.Content} ${contentStyles}`}>{children}</div>
                </div>
            )}
        </div>
    );
};
