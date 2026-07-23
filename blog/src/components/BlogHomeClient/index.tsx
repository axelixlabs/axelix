"use client";
import { useSearchParams } from "next/navigation";
import { Toolbar } from "../Toolbar";
import { PostRow } from "../PostRow";
import { Pagination } from "../Pagination";
import { SHOW_ALL } from "@/lib/tags";
import { PAGE_SIZE } from "@/lib/pagination";
import type { BlogCardItem } from "@/lib/source";
import styles from "./styles.module.css";
import { BlogMeta } from "./BlogMeta";
import { FeaturedPost } from "./FeaturedPost";

function parsePage(value: string | null): number {
  const parsed = Number.parseInt(value ?? "1", 10);
  return Number.isNaN(parsed) || parsed < 1 ? 1 : parsed;
}

interface IProps {
  items: BlogCardItem[];
}

export const BlogHomeClient = ({ items }: IProps) => {
  const searchParams = useSearchParams();

  const allTags = Array.from(new Set(items.flatMap((item) => item.tags))).sort();

  const tagParam = searchParams.get("tag") ?? "";
  const currentTag = allTags.includes(tagParam) ? tagParam : SHOW_ALL;

  const byTag =
    currentTag === SHOW_ALL
      ? items
      : items.filter((item) => item.tags.includes(currentTag));

  const isDefault = currentTag === SHOW_ALL;
  const totalPages = Math.max(1, Math.ceil(byTag.length / PAGE_SIZE));
  const currentPage = Math.max(1, Math.min(parsePage(searchParams.get("page")), totalPages));

  const showFeatured = isDefault && currentPage === 1 && byTag.length > 0;
  const featured = showFeatured ? byTag[0] : undefined;
  const posts = showFeatured
    ? byTag.slice(1, PAGE_SIZE)
    : byTag.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);

  return (
    <>
      <Toolbar currentTag={currentTag} tags={allTags} />
      <main className={styles.PostsWrapper}>
        <div className="wrap">
          <BlogMeta byTag={byTag} currentTag={currentTag} />

          {byTag.length === 0 ? (
            <div className={styles.Empty}>
              <b className={styles.EmptyTitle}>No articles found</b>
              Nothing here yet. Try another topic.
            </div>
          ) : (
            <div>
              {featured && <FeaturedPost item={featured} />}

              <div className={styles.Rowlist}>
                {posts.map((item) => (
                  <PostRow key={item.slug} item={item} />
                ))}
              </div>

              <Pagination
                tag={currentTag}
                currentPage={currentPage}
                totalPages={totalPages}
              />
            </div>
          )}
        </div>
      </main>
    </>
  );
};
