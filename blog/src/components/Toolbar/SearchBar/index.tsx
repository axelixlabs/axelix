"use client"
import styles from "./styles.module.css";
import { MagnifyingGlassIcon } from "@/assets";

interface IProps {
    onClick: () => void;
}

export const SearchBar = ({ onClick }: IProps) => {
    return (
        <>
            <button
                onClick={onClick}
                className={styles.Search}
                aria-label="Search the blog"
            >
                <MagnifyingGlassIcon />

                <span className={styles.Placeholder}>Search the blog…</span>
                <kbd className={styles.KBD}>⌘K</kbd>
            </button>
        </>
    );
};