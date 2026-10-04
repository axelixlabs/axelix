import type { Metadata } from "next";
import Link from "next/link";
import { AuthorCard } from "@/components";
import { getAuthorSummaries } from "@/lib/source";
import { withBlogBasePath } from "@/lib/url";
import styles from "./page.module.css";

const AUTHORS_LEDE =
  "Engineers from the Axelix team and community who write about Java, Spring Boot, and running them in production. Efficiently.";

export const metadata: Metadata = {
  title: "Authors",
  description: AUTHORS_LEDE,
  alternates: { canonical: withBlogBasePath("/authors") },
};

// Fully static — the dataset ships in the RSC payload (see the home page).
export const revalidate = false;

export default async function AuthorsIndexPage() {
  const authors = await getAuthorSummaries();

  return (
    <>
      <header className={styles.Head}>
        <div className="wrap">
          <div className={styles.Eyebrow}>Axelix Blog</div>
          <h1>Authors</h1>
          <p className={styles.Lede}>{AUTHORS_LEDE}</p>
        </div>
      </header>

      <div className={styles.Bar}>
        <div className="wrap">
          <div className={styles.BarInner}>
            <Link href="/" className={styles.Tab}>
              All articles
            </Link>
            <span className={`${styles.Tab} ${styles.TabActive}`}>
              <span className={styles.TabDot} />
              Authors
            </span>
          </div>
        </div>
      </div>

      <main className={styles.Main}>
        <div className="wrap">
          <div className={styles.Count}>
            <b>{authors.length}</b> {authors.length === 1 ? "author" : "authors"}
          </div>
          <div className={styles.Grid}>
            {authors.map((author) => (
              <AuthorCard key={author.slug} author={author} />
            ))}
          </div>
        </div>
      </main>
    </>
  );
}
