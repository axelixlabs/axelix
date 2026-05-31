import Link from "next/link";
import { getPaginationSequence } from "@/lib/pagination";
import { SHOW_ALL } from "@/lib/tags";
import styles from "./styles.module.css";

function hrefFor(tag: string, page: number): string {
  const params = new URLSearchParams();
  if (tag !== SHOW_ALL) params.set("tag", tag);
  if (page > 1) params.set("page", String(page));
  const query = params.toString();
  return query ? `/?${query}` : "/";
}

interface IProps {
  tag: string;
  currentPage: number;
  totalPages: number;
}

const PREV_ICON = (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M15 6l-6 6l6 6" />
  </svg>
);
const NEXT_ICON = (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M9 6l6 6l-6 6" />
  </svg>
);

export const Pagination = ({ tag, currentPage, totalPages }: IProps) => {
  if (totalPages <= 1) return null;
  const sequence = getPaginationSequence(totalPages, currentPage);
  const prevDisabled = currentPage <= 1;
  const nextDisabled = currentPage >= totalPages;

  return (
    <nav className={styles.Pager} aria-label="Pagination">
      {prevDisabled ? (
        <span className={styles.PgArrow} aria-disabled="true">
          {PREV_ICON}
          <span>Prev</span>
        </span>
      ) : (
        <Link className={styles.PgArrow} href={hrefFor(tag, currentPage - 1)} aria-label="Previous page">
          {PREV_ICON}
          <span>Prev</span>
        </Link>
      )}

      <div className={styles.PgNums}>
        {sequence.map((entry, index) =>
          entry === "ellipsis" ? (
            <span key={`e-${index}`} className={styles.PgEllipsis}>
              …
            </span>
          ) : (
            <Link
              key={entry}
              className={`${styles.PgNum}${entry === currentPage ? ` ${styles.Active}` : ""}`}
              href={hrefFor(tag, entry)}
              aria-current={entry === currentPage ? "page" : undefined}
            >
              {String(entry).padStart(2, "0")}
            </Link>
          ),
        )}
      </div>

      {nextDisabled ? (
        <span className={styles.PgArrow} aria-disabled="true">
          <span>Next</span>
          {NEXT_ICON}
        </span>
      ) : (
        <Link className={styles.PgArrow} href={hrefFor(tag, currentPage + 1)} aria-label="Next page">
          <span>Next</span>
          {NEXT_ICON}
        </Link>
      )}
    </nav>
  );
};
