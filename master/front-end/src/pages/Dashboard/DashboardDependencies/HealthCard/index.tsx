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
import { type ReactNode } from "react";

import styles from "./styles.module.css";

interface IProps {
    eyebrow: string;
    title: string;
    descriptor: ReactNode;
    children: ReactNode;
    footerLeft: ReactNode;
    footerRight: string;
}

export const HealthCard = ({ eyebrow, title, descriptor, children, footerLeft, footerRight }: IProps) => {
    return (
        <>
            <div className={styles.Card}>
                <div className={styles.Header}>
                    <div className={`TextUltraSmall ${styles.Eyebrow}`}>{eyebrow}</div>
                    <div className={styles.TitleRow}>
                        <div className={styles.Title}>{title}</div>
                        <div className={`TextUltraSmall ${styles.Descriptor}`}>{descriptor}</div>
                    </div>
                </div>

                {children}

                <div className={`TextUltraSmall ${styles.FooterWrapper}`}>
                    <div>{footerLeft}</div>
                    <div className={styles.FooterMono}>{footerRight}</div>
                </div>
            </div>
        </>
    );
};
