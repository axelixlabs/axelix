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
"use client";
import { useEffect, useRef, useState } from "react";

import { ENTERPRISE_HREF, HEADER_MENUS, getLinkTargetProps } from "../../../utils";
import { ExternalLinks } from "../ExternalLinks";

import { BurgerButton } from "./BurgerButton";
import styles from "./styles.module.css";

const MOBILE_MENUS = HEADER_MENUS.map(({ key, label, columns }) => ({
    key,
    label,
    links: columns.flatMap(({ links }) => links),
}));

export const BurgerMenu = () => {
    const [isOpen, setIsOpen] = useState<boolean>(false);
    const wrapperRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!isOpen) {
            return;
        }

        const handleClickOutside = (e: MouseEvent) => {
            if (wrapperRef.current && !wrapperRef.current.contains(e.target as Node)) {
                setIsOpen(false);
            }
        };

        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [isOpen]);

    const closeMenu = () => setIsOpen(false);

    return (
        <div ref={wrapperRef} className={styles.MainWrapper}>
            <BurgerButton
                isOpen={isOpen}
                onClick={() => {
                    setIsOpen((prev) => !prev);
                }}
            />

            {isOpen && (
                <div className={styles.MobileMenu}>
                    <nav className={styles.NavLinks}>
                        {MOBILE_MENUS.map(({ key, label, links }) => (
                            <div key={key} className={styles.Group}>
                                <span className={styles.GroupHeading}>{label}</span>
                                {links.map(({ title, href, external }) => (
                                    <a
                                        key={title}
                                        href={href}
                                        className={styles.NavLink}
                                        onClick={closeMenu}
                                        {...getLinkTargetProps(external)}
                                    >
                                        {title}
                                    </a>
                                ))}
                            </div>
                        ))}
                        <a href={ENTERPRISE_HREF} className={styles.NavLink} onClick={closeMenu}>
                            Enterprise
                        </a>
                    </nav>
                    <div className={styles.Divider} />
                    <ExternalLinks onLinkClick={closeMenu} fullWidth />
                </div>
            )}
        </div>
    );
};
