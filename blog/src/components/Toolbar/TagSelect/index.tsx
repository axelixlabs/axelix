"use client"
import styles from "./styles.module.css"
import { useEffect, useRef, useState } from "react";
import { colorForTag, SHOW_ALL } from "@/lib/tags";
import { ChevronIcon } from "@/assets";
import Link from "next/link";
import { chipColorStyle } from "@/helpers";
import { DEFAULT_CHIP_STYLE } from "@/utils";

interface IProps {
    tags: string[];
    currentTag: string;
}

export const TagSelect = ({ tags, currentTag }: IProps) => {
    const [open, setOpen] = useState<boolean>(false);
    const selectRef = useRef<HTMLDivElement>(null);

    const options = [
        { id: SHOW_ALL, label: "All", href: "/" },
        ...tags.map((tag) => ({
            id: tag,
            label: tag,
            href: `/?tag=${encodeURIComponent(tag)}`
        })),
    ];

    const current =
        currentTag === SHOW_ALL
            ? { id: SHOW_ALL, label: "All" }
            : { id: currentTag, label: currentTag };

    useEffect(() => {
        if (!open) {
            return;
        }

        function onPointerDown(e: MouseEvent): void {
            const target = e.target as Node;

            if (selectRef.current && !selectRef.current.contains(target)) {
                setOpen(false);
            }
        }

        function onKeyDown(e: KeyboardEvent): void {
            if (e.key === "Escape") {
                setOpen(false);
            }
        }

        document.addEventListener("mousedown", onPointerDown);
        document.addEventListener("keydown", onKeyDown);

        return () => {
            document.removeEventListener("mousedown", onPointerDown);
            document.removeEventListener("keydown", onKeyDown);
        };
    }, [open]);

    return (
        <div className={`${styles.MainWrapper}${open ? ` ${styles.Open}` : ""}`} ref={selectRef}>
            <button
                type="button"
                className={styles.SelectOpenTrigger}
                aria-haspopup="menu"
                aria-expanded={open}
                onClick={() => setOpen((value) => !value)}
                style={current.id === SHOW_ALL ? DEFAULT_CHIP_STYLE : chipColorStyle(colorForTag(current.id))}
            >
                <span className={styles.ChipDot} />
                <span className={styles.Label}>{current.label}</span>
                <ChevronIcon />
            </button>

            {open && (
                <div role="menu">
                    {options.map(({ id, label, href }) => (
                        <Link
                            key={id}
                            href={href}
                            role="menuitem"
                            className={`${styles.SelectOption}${current.id === id ? styles.Active : ""}`}
                            style={id === SHOW_ALL ? DEFAULT_CHIP_STYLE : chipColorStyle(colorForTag(id))}
                            onClick={() => setOpen(false)}
                        >
                            <span className={styles.ChipDot} />
                            <span className={styles.Lbl}>{label}</span>
                        </Link>
                    ))}
                </div>
            )}
        </div>
    )
}