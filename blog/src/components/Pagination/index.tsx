import Link from "next/link";
import { getPaginationSequence } from "@/lib/pagination";
import { SHOW_ALL } from "@/lib/tags";
import styles from "./styles.module.css";
import { PaginationArrowIcon } from "@/assets";

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

export const Pagination = ({ tag, currentPage, totalPages }: IProps) => {
  if (totalPages <= 1) {
    return null;
  }

  const sequence = getPaginationSequence(totalPages, currentPage);
  const prevDisabled = currentPage <= 1;
  const nextDisabled = currentPage >= totalPages;

  return (
    <nav className={styles.MainWrapper} aria-label="Pagination">
      {prevDisabled ? (
        <span className={styles.PaginationArrow} aria-disabled="true">
          <PaginationArrowIcon className={styles.PaginationArrowPrevIcon} />
          <span className={styles.PaginationPrevNextText}>Prev</span>
        </span>
      ) : (
        <Link className={styles.PaginationArrow} href={hrefFor(tag, currentPage - 1)} aria-label="Previous page">
          <PaginationArrowIcon className={styles.PaginationArrowPrevIcon} />
          <span className={styles.PaginationPrevNextText}>Prev</span>
        </Link>
      )}

      <div className={styles.PaginationNumbers}>
        {sequence.map((entry, index) =>
          entry === "ellipsis" ? (
            <span key={index} className={styles.PaginationEllipsis}>
              …
            </span>
          ) : (
            <Link
              key={entry}
              className={`${styles.PaginationNumber}${entry === currentPage ? ` ${styles.ActivePaginationNumber}` : ""}`}
              href={hrefFor(tag, entry)}
              aria-current={entry === currentPage ? "page" : undefined}
            >
              {String(entry).padStart(2, "0")}
            </Link>
          ),
        )}
      </div>

      {nextDisabled ? (
        <span className={styles.PaginationArrow} aria-disabled="true">
          <span className={styles.PaginationPrevNextText}>Next</span>
          <PaginationArrowIcon />
        </span>
      ) : (
        <Link className={styles.PaginationArrow} href={hrefFor(tag, currentPage + 1)} aria-label="Next page">
          <span className={styles.PaginationPrevNextText}>Next</span>
          <PaginationArrowIcon />
        </Link>
      )}
    </nav>
  );
};
