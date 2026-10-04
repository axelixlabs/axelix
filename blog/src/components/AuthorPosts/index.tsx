"use client";

import { type CSSProperties } from "react";
import Link from "next/link";
import { usePathname, useSearchParams } from "next/navigation";
import { PostRow } from "../PostRow";
import { SHOW_ALL, colorForTag } from "@/lib/tags";
import type { BlogCardItem } from "@/lib/source";
import styles from "./styles.module.css";

interface IProps {
  posts: BlogCardItem[];
  /** The author's first name, used in the heading and empty state. */
  firstName: string;
}

const dotStyle = (color: string): CSSProperties => ({ ["--cat" as string]: color });

/** The author profile's article section: an optional tag filter (only when the
 *  author has more than one article) driven by the `?tag=` URL param, plus the
 *  filtered rows. Mirrors the home feed's URL-based filtering. */
export const AuthorPosts = ({ posts, firstName }: IProps) => {
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const allTags = Array.from(new Set(posts.flatMap((post) => post.tags))).sort();
  const tagParam = searchParams.get("tag") ?? "";
  const activeTag = allTags.includes(tagParam) ? tagParam : SHOW_ALL;

  const filtered =
    activeTag === SHOW_ALL ? posts : posts.filter((post) => post.tags.includes(activeTag));
  const showFilters = posts.length > 1 && allTags.length > 0;

  const chipHref = (tag: string) =>
    tag === SHOW_ALL ? pathname : `${pathname}?tag=${encodeURIComponent(tag)}`;

  const chip = (tag: string, label: string, color: string) => {
    const active = tag === activeTag;
    return (
      <Link
        key={tag}
        href={chipHref(tag)}
        className={`${styles.Chip}${active ? ` ${styles.Active}` : ""}`}
        style={dotStyle(active ? "#8fbd4b" : color)}
      >
        <span className={styles.Cdot} />
        {label}
      </Link>
    );
  };

  return (
    <>
      <div className={styles.Head}>
        <h2 className={styles.Title}>Articles by {firstName}</h2>
        {showFilters && (
          <div className={styles.Filters}>
            {chip(SHOW_ALL, "All", "var(--ink-4)")}
            {allTags.map((tag) => chip(tag, tag, colorForTag(tag)))}
          </div>
        )}
      </div>

      {filtered.length === 0 ? (
        <div className={styles.Empty}>
          <b>No articles yet</b>
          {firstName}&rsquo;s first article will appear here once it is published.
        </div>
      ) : (
        <div className={styles.Rows}>
          {filtered.map((post) => (
            <PostRow key={post.slug} item={post} hideAuthors />
          ))}
        </div>
      )}
    </>
  );
};
