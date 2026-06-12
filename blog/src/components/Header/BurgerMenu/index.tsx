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

import { NAV_LINKS } from "../../../utils";
import { ExternalLinks } from "../ExternalLinks";

import { BurgerButton } from "./BurgerButton";
import styles from "./styles.module.css";

export const BurgerMenu = () => {
    const [isOpen, setIsOpen] = useState<boolean>(false);
    const [hash, setHash] = useState<string>("");
    const wrapperRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const updateHash = (): void => {
            setHash(window.location.hash);
        };

        updateHash();

        window.addEventListener("hashchange", updateHash);

        return () => {
            window.removeEventListener("hashchange", updateHash);
        };
    }, []);

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
                        {NAV_LINKS.map(({ href, label }) => (
                            <a
                                key={href}
                                href={href}
                                className={`${styles.NavLink} ${hash === href ? styles.NavLinkActive : ""}`}
                                onClick={closeMenu}
                            >
                                {label}
                            </a>
                        ))}
                    </nav>
                    <div className={styles.Divider} />
                    <ExternalLinks onLinkClick={closeMenu} fullWidth />
                </div>
            )}
        </div>
    );
};
