"use client";
import styles from "./styles.module.css";
import { useEffect, useState } from "react";

const NAV_LINKS = [
    { href: "#capabilities", label: "Capabilities" },
    { href: "#install", label: "Install" },
    { href: "#enterprise", label: "Enterprise" },
    { href: "#faq", label: "FAQ" },
];

export const NavLinks = () => {
    const [hash, setHash] = useState<string>("")

    useEffect(() => {
        const updateHash = () => {
            setHash(window.location.hash)
        }

        updateHash()

        window.addEventListener('hashchange', updateHash)

        return () => {
            window.removeEventListener('hashchange', updateHash)
        }
    }, [])

    return (
        <div className={styles.LinksWrapper}>
            {NAV_LINKS.map(({ href, label }) => (
                <a
                    key={href}
                    href={href}
                    className={`${styles.Link} ${hash === href ? styles.ActiveLink : ""}`}
                >
                    {label}
                </a>
            ))}
        </div>
    );
};