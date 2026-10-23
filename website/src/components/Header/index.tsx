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
import { ChevronDownIcon, LogoIcon } from "@/assets";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useRef, useState } from "react";

import { ENTERPRISE_HREF, HEADER_MENUS } from "../../utils";

import { BurgerMenu } from "./BurgerMenu";
import { CompactMenu } from "./CompactMenu";
import { ExternalLinks } from "./ExternalLinks";
import styles from "./styles.module.css";

// Grace period before the menu closes once the pointer leaves the header.
const CLOSE_DELAY_MS = 180;

export const Header = () => {
    const [openKey, setOpenKey] = useState<string | null>(null);
    const pathname = usePathname();
    const headerRef = useRef<HTMLElement>(null);
    const closeTimer = useRef<number | undefined>(undefined);

    const cancelClose = () => window.clearTimeout(closeTimer.current);

    const openMenu = (key: string) => {
        cancelClose();
        setOpenKey(key);
    };

    const closeMenu = (delay: number) => {
        cancelClose();
        closeTimer.current = window.setTimeout(() => setOpenKey(null), delay);
    };

    useEffect(() => {
        if (!openKey) {
            return;
        }

        const handleKeyDown = (e: KeyboardEvent) => {
            if (e.key === "Escape") {
                setOpenKey(null);
            }
        };

        const handlePointerDown = (e: PointerEvent) => {
            if (headerRef.current && !headerRef.current.contains(e.target as Node)) {
                setOpenKey(null);
            }
        };

        document.addEventListener("keydown", handleKeyDown);
        document.addEventListener("pointerdown", handlePointerDown);
        return () => {
            document.removeEventListener("keydown", handleKeyDown);
            document.removeEventListener("pointerdown", handlePointerDown);
        };
    }, [openKey]);

    useEffect(() => () => window.clearTimeout(closeTimer.current), []);

    // The menu that links to the current page stays underlined.
    const activeKey = HEADER_MENUS.find(({ columns }) =>
        columns.flatMap(({ links }) => links).some(({ href }) => href === pathname),
    )?.key;

    return (
        <header
            ref={headerRef}
            className={styles.MainWrapper}
            onMouseEnter={cancelClose}
            onMouseLeave={() => closeMenu(CLOSE_DELAY_MS)}
            onBlur={(e) => {
                if (!e.currentTarget.contains(e.relatedTarget)) {
                    setOpenKey(null);
                }
            }}
        >
            <div className={`WideContainer ${styles.Bar}`}>
                <Link href="/" aria-label="Axelix home" onMouseEnter={() => closeMenu(0)}>
                    <LogoIcon className={styles.LogoIcon} />
                </Link>
                <nav className={styles.Nav}>
                    {HEADER_MENUS.map((menu) => {
                        const { key, label } = menu;
                        const isOpen = key === openKey;

                        return (
                            <div key={key} className={styles.NavItemWrapper}>
                                <button
                                    type="button"
                                    aria-expanded={isOpen}
                                    className={`${styles.NavItem} ${isOpen ? styles.NavItemOpen : ""} ${key === activeKey ? styles.NavItemActive : ""}`}
                                    onMouseEnter={() => openMenu(key)}
                                    onClick={() => openMenu(key)}
                                >
                                    {label}
                                    <ChevronDownIcon className={styles.Caret} />
                                </button>
                                {isOpen && <CompactMenu menu={menu} onLinkClick={() => setOpenKey(null)} />}
                            </div>
                        );
                    })}
                    <Link
                        href={ENTERPRISE_HREF}
                        className={`${styles.NavItem} ${pathname === ENTERPRISE_HREF ? styles.NavItemActive : ""}`}
                        onMouseEnter={() => closeMenu(0)}
                    >
                        Enterprise
                    </Link>
                </nav>
                <div className={styles.ExternalLinksWrapper} onMouseEnter={() => closeMenu(0)}>
                    <ExternalLinks />
                </div>
                <BurgerMenu />
            </div>
        </header>
    );
};
