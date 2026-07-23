"use client"
import {colorForTag} from "@/lib/tags";
import {ChevronIcon} from "@/assets";
import Link from "next/link";

import styles from "./styles.module.css"
import {useEffect, useRef, useState} from "react";
import {VISIBLE_TAG_COUNT} from "@/utils";
import {chipColorStyle} from "@/helpers";

interface IProps {
    tags: string[];
    currentTag: string;
}

export const MoreTags = ({ tags, currentTag }: IProps) => {
    const [moreOpen, setMoreOpen] = useState<boolean>(false);
    const moreRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!moreOpen) {
            return;
        }

        function onPointerDown(e: MouseEvent): void {
            const target = e.target as Node;

            if (moreRef.current && !moreRef.current.contains(target)) {
                setMoreOpen(false);
            }
        }

        function onKeyDown(e: KeyboardEvent): void {
            if (e.key === "Escape") {
                setMoreOpen(false);
            }
        }

        document.addEventListener("mousedown", onPointerDown);
        document.addEventListener("keydown", onKeyDown);

        return () => {
            document.removeEventListener("mousedown", onPointerDown);
            document.removeEventListener("keydown", onKeyDown);
        };
    }, [moreOpen]);

    const overflowTags = tags.slice(VISIBLE_TAG_COUNT);
    const activeOverflow = overflowTags.find((tag) => tag === currentTag);

    return (
        <>
            {!!overflowTags.length && (
                <div
                    className={`${styles.MenuWrapper}${moreOpen ? ` ${styles.MenuOpen}` : ""}`}
                    ref={moreRef}
                >
                    <button
                        className={`${styles.Chip} ${activeOverflow ? ` ${styles.Active}` : ""}`}
                        aria-haspopup="menu"
                        aria-expanded={moreOpen}
                        onClick={() => setMoreOpen((value) => !value)}
                        style={activeOverflow ? chipColorStyle(colorForTag(activeOverflow)) : undefined}
                    >
                        {activeOverflow && <span className={styles.ChipDot}/>}
                        {activeOverflow ?? "More"}
                        <ChevronIcon/>
                    </button>

                    {moreOpen && (
                        <div className={styles.Menu} role="menu">
                            {overflowTags.map((tag) => (
                                <Link
                                    key={tag}
                                    href={`/?tag=${encodeURIComponent(tag)}`}
                                    role="menuitem"
                                    className={`${styles.MenuOption} ${currentTag === tag ? ` ${styles.Active}` : ""}`}
                                    style={chipColorStyle(colorForTag(tag))}
                                    onClick={() => setMoreOpen(false)}
                                >
                                    <span className={styles.ChipDot}/>
                                    <span className={styles.Lbl}>{tag}</span>
                                </Link>
                            ))}
                        </div>
                    )}
                </div>
            )}
        </>
    )
}
