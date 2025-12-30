/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        <div className={`${styles.MainWrapper} ${open ? styles.Open : ""}`}>
            <div className={`${styles.HeaderWrapper} ${headerStyles}`} onClick={handlerClick}>
                {!hideArrowIcon && <img src={ArrowIcon} alt="Arrow icon" className={styles.Icon} />}
                <div className={styles.Header}>{header}</div>
            </div>
            <div className={styles.ContentWrapper}>
                <div className={`${styles.Content} ${contentStyles}`}>{children}</div>
            </div>
        </div>
    );
};
