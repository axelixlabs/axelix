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
import { type PropsWithChildren, type ReactNode, useEffect, useRef, useState } from "react";

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
     * CSS classes for the accordion content
     */
    contentStyles?: string;

    /**
     * CSS classes for the main accordion wrapper
     */
    wrapperStyles?: string;

    /**
     * CSS classes for the content main wrapper
     */
    contentWrapperStyles?: string;

    /**
     * Indicates whether the accordion is expanded
     */
    accordionExpanded?: boolean;

    /**
     * If true, the arrow icon will not be displayed
     */
    hideArrowIcon?: boolean;

    /**
     * Function triggered when the accordion is closed
     */
    onClose?: () => void;
}

export const Accordion = ({
    header,
    children,
    wrapperStyles,
    headerStyles,
    contentStyles,
    contentWrapperStyles,
    accordionExpanded = false,
    hideArrowIcon = false,
    onClose,
}: PropsWithChildren<IProps>) => {
    const [open, setOpen] = useState<boolean>(accordionExpanded);
    const [isContentMounted, setIsContentMounted] = useState<boolean>(accordionExpanded);
    const timeoutRef = useRef<ReturnType<typeof setTimeout>>(null);

    useEffect(() => {
        return () => {
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, []);

    /*
     * We use the isContentMounted pattern instead of a pure CSS approach (e.g. overflow: hidden)
     * because accordion children can be heavy components that perform HTTP requests (e.g. metrics).
     * In large lists, mounting all children at once would trigger a huge number of unnecessary requests
     * for accordions the user never opens. By controlling isContentMounted, children are only mounted
     * when the user explicitly opens the accordion, and unmounted after it closes.
     *
     * On open:  mount children first, then trigger animation after 10ms (one render cycle)
     * On close: trigger animation immediately, then unmount children after 300ms (animation duration)
     */
    const handlerClick = (): void => {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }

        if (open) {
            setOpen(false);

            if (onClose) {
                onClose();
            }

            timeoutRef.current = setTimeout(() => {
                setIsContentMounted(false);
            }, 300);
        } else {
            setIsContentMounted(true);

            timeoutRef.current = setTimeout(() => {
                setOpen(true);
            }, 10);
        }
    };

    return (
        <>
            <div className={`${styles.MainWrapper} ${wrapperStyles} ${open ? styles.Open : ""}`}>
                <div
                    className={`${styles.HeaderWrapper} ${headerStyles} ${hideArrowIcon ? styles.HideArrow : ""}`}
                    onClick={handlerClick}
                >
                    {header}
                </div>
                <div
                    className={`${styles.ContentWrapper} ${open ? styles.ContentWrapperOpened : ""} ${contentWrapperStyles}`}
                >
                    {isContentMounted && (
                        <div className={styles.ContentInner}>
                            <div className={`${styles.Content} ${contentStyles}`}>{children}</div>
                        </div>
                    )}
                </div>
            </div>
        </>
    );
};
