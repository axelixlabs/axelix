import type { ReactNode } from "react";
import { TopNav, SiteFooter } from "@/components";
import styles from "./layout.module.css";

export default function BlogLayout({ children }: { children: ReactNode }) {
  return (
    <>
      <TopNav />
      <div className={styles.Page}>{children}</div>
      <SiteFooter />
    </>
  );
}
