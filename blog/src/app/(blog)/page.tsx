import { Suspense } from "react";
import { BlogHomeClient } from "@/components";
import { getSortedCardItems } from "@/lib/source";
import { BLOG_HOME_DESCRIPTION } from "@/lib/blog-metadata";
import styles from "./page.module.css";

export const revalidate = false;

export default async function HomePage() {
  const items = await getSortedCardItems();

  return (
    <>
      <header className={styles.Hero}>
        <div className="wrap">
          <h1 className={styles.Title}>
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
