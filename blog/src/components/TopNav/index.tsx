import Link from "next/link";
import { Logo } from "../Logo";
import { GITHUB_URL } from "@/lib/blog-metadata";
import styles from "./styles.module.css";

export const TopNav = () => {
  return (
    <nav className={styles.Top}>
      <div className={styles.Capsule}>
        <a href="https://axelix.io/" className={styles.Brand} aria-label="Axelix">
          <Logo />
        </a>
        <div className={styles.NavLinks}>
          <a href="https://axelix.io/#reference-app" className={styles.NavLink}>
            Why Axelix?
          </a>
          <a href="https://axelix.io/#capabilities" className={styles.NavLink}>
            Debugging
          </a>
          <a href="https://axelix.io/#install" className={styles.NavLink}>
            Install
          </a>
          <a href="https://axelix.io/#enterprise" className={styles.NavLink}>
            Enterprise
          </a>
          <a href="https://axelix.io/#faq" className={styles.NavLink}>
            FAQ
          </a>
        </div>
        <div className={styles.NavCta}>
          <a
            href="https://axelix.io/docs/product/introduction"
            className={styles.ExtLink}
            target="_blank"
            rel="noopener noreferrer"
          >
            Docs
          </a>
          <Link href="/" className={`${styles.ExtLink} ${styles.Active}`}>
            Blog
          </Link>
          <a href={GITHUB_URL} className={styles.GhBtn} target="_blank" rel="noopener noreferrer">
            <svg
              width="11"
              height="11"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.7"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M9 19c-4.3 1.4 -4.3 -2.5 -6 -3m12 5v-3.5c0 -1 .1 -1.4 -.5 -2c2.8 -.3 5.5 -1.4 5.5 -6a4.6 4.6 0 0 0 -1.3 -3.2a4.2 4.2 0 0 0 -.1 -3.2s-1.1 -.3 -3.5 1.3a12.3 12.3 0 0 0 -6.2 0c-2.4 -1.6 -3.5 -1.3 -3.5 -1.3a4.2 4.2 0 0 0 -.1 3.2a4.6 4.6 0 0 0 -1.3 3.2c0 4.6 2.7 5.7 5.5 6c-.6 .6 -.6 1.2 -.5 2v3.5" />
            </svg>
            GitHub
          </a>
        </div>
      </div>
    </nav>
  );
};
