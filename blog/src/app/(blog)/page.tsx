import { Suspense } from "react";
import { BlogHomeClient } from "@/components";
import { getSortedCardItems } from "@/lib/source";
import { BLOG_HOME_DESCRIPTION } from "@/lib/blog-metadata";
import styles from "./page.module.css";

// Fully static: we never read `searchParams` here (that would force dynamic
// rendering with `Cache-Control: private`). Filtering/pagination happen on the
// client from the URL; the whole dataset ships in the RSC payload.
export const revalidate = false;

export default async function HomePage() {
  const items = await getSortedCardItems();

  return (
    <>
      <header className={styles.BlogHero}>
        <div className="wrap">
          <h1>
            Axelix <span className={styles.Accent}>Blog</span>
          </h1>
          <p className={styles.Lede}>{BLOG_HOME_DESCRIPTION}</p>
        </div>
      </header>
      <Suspense>
        <BlogHomeClient items={items} />
      </Suspense>
    </>
  );
}
