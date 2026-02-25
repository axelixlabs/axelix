/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import { type PropsWithChildren, type ReactNode, useState } from "react";

import styles from "./styles.module.css";

import { ArrowIcon } from "assets";

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
     * CSS classes for the main accordion wrapper.
     */
    wrapperStyles?: string;

    /**
     * Indicates whether the accordion is expanded
     */
    accordionExpanded?: boolean;

    /**
     * If true, the arrow icon will not be displayed.
     */
    hideArrowIcon?: boolean;

    /**
     * Function triggered when the accordion is closed.
     */
    onClose?: () => void;
}

export const Accordion = ({
    header,
    children,
    wrapperStyles,
    headerStyles,
    contentStyles,
    accordionExpanded = false,
    hideArrowIcon = false,
    onClose,
}: PropsWithChildren<IProps>) => {
    const [open, setOpen] = useState<boolean>(accordionExpanded);

    const handlerClick = (): void => {
        if (open && onClose) {
            onClose();
        }

        setOpen(!open);
    };

    return (
        <>
            <div className={`${styles.MainWrapper} ${wrapperStyles} ${open ? styles.Open : ""}`}>
                <div className={`${styles.HeaderWrapper} ${headerStyles}`} onClick={handlerClick}>
                    {!hideArrowIcon && <ArrowIcon className={styles.Icon} />}
                    <div className={styles.Header}>{header}</div>
                </div>
                {open && (
                    <div className={styles.ContentWrapper}>
                        <div className={`${styles.Content} ${contentStyles}`}>{children}</div>
                    </div>
                )}
            </div>
        </>
    );
};
