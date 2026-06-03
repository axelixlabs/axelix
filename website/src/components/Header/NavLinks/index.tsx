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
import { useEffect, useState } from "react";

import styles from "./styles.module.css";

const NAV_LINKS = [
    { href: "#capabilities", label: "Capabilities" },
    { href: "#install", label: "Install" },
    { href: "#enterprise", label: "Enterprise" },
    { href: "#faq", label: "FAQ" },
];

export const NavLinks = () => {
    const [hash, setHash] = useState<string>("");

    useEffect(() => {
        const updateHash = () => {
            setHash(window.location.hash);
        };

        updateHash();

        window.addEventListener("hashchange", updateHash);

        return () => {
            window.removeEventListener("hashchange", updateHash);
        };
    }, []);

    return (
        <div className={styles.LinksWrapper}>
            {NAV_LINKS.map(({ href, label }) => (
                <a key={href} href={href} className={`${styles.Link} ${hash === href ? styles.ActiveLink : ""}`}>
                    {label}
                </a>
            ))}
        </div>
    );
};
