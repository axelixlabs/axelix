export const PAGE_SIZE = 10;

/**
 * Builds a pagination sequence like `[1, "ellipsis", 4, 5, 6, "ellipsis", 20]`.
 * For 7 or fewer pages every page is listed.
 */
export function getPaginationSequence(
  totalPages: number,
  currentPage: number,
): Array<number | "ellipsis"> {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, index) => index + 1);
  }

  const pages: Array<number | "ellipsis"> = [1];
  const start = Math.max(2, currentPage - 1);
  const end = Math.min(totalPages - 1, currentPage + 1);

  if (start > 2) pages.push("ellipsis");
  for (let page = start; page <= end; page += 1) pages.push(page);
  if (end < totalPages - 1) pages.push("ellipsis");

  pages.push(totalPages);
  return pages;
}
