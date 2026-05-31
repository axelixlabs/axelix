"use client";

import { useEffect, useRef, useState, type CSSProperties } from "react";
import Link from "next/link";
import { useSearchContext } from "fumadocs-ui/contexts/search";
import { SHOW_ALL, colorForTag } from "@/lib/tags";
import styles from "./styles.module.css";

interface IProps {
  currentTag: string;
  /** The filter vocabulary — derived from the posts (see `BlogHomeClient`). */
  tags: string[];
}

/** Drives the chip's per-tag color via the `--cat` CSS var. */
const dotStyle = (color: string): CSSProperties => ({ ["--cat" as string]: color });

/** "All" is the no-filter sentinel, not a tag — render it neutral grey. */
const ALL_STYLE = dotStyle("var(--ink-4)");

/** How many tags stay as desktop pills; the rest collapse into a "More" menu. */
const VISIBLE_TAG_COUNT = 6;

const CHEVRON = (
  <svg
    className={styles.Chev}
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="m6 9 6 6 6-6" />
  </svg>
);

export const Toolbar = ({ currentTag, tags }: IProps) => {
  const { setOpenSearch } = useSearchContext();
  const [open, setOpen] = useState(false); // mobile selector
  const [moreOpen, setMoreOpen] = useState(false); // desktop overflow menu
  const selectRef = useRef<HTMLDivElement>(null);
  const moreRef = useRef<HTMLDivElement>(null);

  // Close whichever tag menu is open on outside click / Escape.
  useEffect(() => {
    if (!open && !moreOpen) return;
    function onPointerDown(e: MouseEvent) {
      const target = e.target as Node;
      if (selectRef.current && !selectRef.current.contains(target)) setOpen(false);
      if (moreRef.current && !moreRef.current.contains(target)) setMoreOpen(false);
    }
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === "Escape") {
        setOpen(false);
        setMoreOpen(false);
      }
    }
    document.addEventListener("mousedown", onPointerDown);
    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("mousedown", onPointerDown);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, [open, moreOpen]);

  const visibleTags = tags.slice(0, VISIBLE_TAG_COUNT);
  const overflowTags = tags.slice(VISIBLE_TAG_COUNT);

  // All + tags, in display order, for the mobile selector.
  const options = [
    { id: SHOW_ALL, label: "All", href: "/" },
    ...tags.map((t) => ({ id: t, label: t, href: `/?tag=${encodeURIComponent(t)}` })),
  ];

  const current =
    currentTag === SHOW_ALL
      ? { id: SHOW_ALL, label: "All" }
      : { id: currentTag, label: currentTag };

  // When an overflow tag is the active filter, surface it on the "More" trigger.
  const activeOverflow = overflowTags.find((t) => t === currentTag);

  return (
    <div className={styles.Toolbar}>
      <div className="wrap">
        <div className={styles.Row}>
          {/* Desktop: All + first N tag pills, the rest in a "More" menu. */}
          <div className={styles.Cats}>
            <Link
              href="/"
              className={`${styles.Cat}${currentTag === SHOW_ALL ? ` ${styles.Active}` : ""}`}
              style={ALL_STYLE}
            >
              <span className={styles.Cdot} />
              All
            </Link>
            {visibleTags.map((t) => (
              <Link
                key={t}
                href={`/?tag=${encodeURIComponent(t)}`}
                className={`${styles.Cat}${currentTag === t ? ` ${styles.Active}` : ""}`}
                style={dotStyle(colorForTag(t))}
              >
                <span className={styles.Cdot} />
                {t}
              </Link>
            ))}

            {overflowTags.length > 0 && (
              <div className={`${styles.CatMore}${moreOpen ? ` ${styles.Open}` : ""}`} ref={moreRef}>
                <button
                  type="button"
                  className={`${styles.Cat} ${styles.CatMoreTrigger}${activeOverflow ? ` ${styles.Active}` : ""}`}
                  aria-haspopup="menu"
                  aria-expanded={moreOpen}
                  onClick={() => setMoreOpen((v) => !v)}
                  style={activeOverflow ? dotStyle(colorForTag(activeOverflow)) : undefined}
                >
                  {activeOverflow && <span className={styles.Cdot} />}
                  {activeOverflow ?? "More"}
                  {CHEVRON}
                </button>
                {moreOpen && (
                  <div className={styles.CatMenu} role="menu">
                    {overflowTags.map((t) => (
                      <Link
                        key={t}
                        href={`/?tag=${encodeURIComponent(t)}`}
                        role="menuitem"
                        className={`${styles.CatOpt}${currentTag === t ? ` ${styles.Active}` : ""}`}
                        style={dotStyle(colorForTag(t))}
                        onClick={() => setMoreOpen(false)}
                      >
                        <span className={styles.Cdot} />
                        <span className={styles.Lbl}>{t}</span>
                      </Link>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Mobile: tag dropdown selector (replaces the pill row). */}
          <div className={`${styles.CatSelect}${open ? ` ${styles.Open}` : ""}`} ref={selectRef}>
            <button
              type="button"
              className={styles.CatTrigger}
              aria-haspopup="menu"
              aria-expanded={open}
              onClick={() => setOpen((v) => !v)}
              style={current.id === SHOW_ALL ? ALL_STYLE : dotStyle(colorForTag(current.id))}
            >
              <span className={styles.Cdot} />
              <span className={styles.Lbl}>{current.label}</span>
              {CHEVRON}
            </button>
            {open && (
              <div className={styles.CatMenu} role="menu">
                {options.map((o) => (
                  <Link
                    key={o.id}
                    href={o.href}
                    role="menuitem"
                    className={`${styles.CatOpt}${current.id === o.id ? ` ${styles.Active}` : ""}`}
                    style={o.id === SHOW_ALL ? ALL_STYLE : dotStyle(colorForTag(o.id))}
                    onClick={() => setOpen(false)}
                  >
                    <span className={styles.Cdot} />
                    <span className={styles.Lbl}>{o.label}</span>
                  </Link>
                ))}
              </div>
            )}
          </div>

          <div className={styles.Tools}>
            <button
              type="button"
              className={styles.Search}
              onClick={() => setOpenSearch(true)}
              aria-label="Search the blog"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="7" />
                <path d="m21 21-4.3-4.3" />
              </svg>
              <span className={styles.Ph}>Search the blog…</span>
              <kbd>⌘K</kbd>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
