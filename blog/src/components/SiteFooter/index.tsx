import { Logo } from "../Logo";
import { GITHUB_URL } from "@/lib/blog-metadata";
import styles from "./styles.module.css";

export const SiteFooter = () => {
  return (
    <footer className={styles.Footer}>
      <div className="wrap">
        <div className={styles.FooterTop}>
          <div className={styles.BrandSide}>
            <a className={styles.FooterBrand} href="https://axelix.io" aria-label="Axelix">
              <Logo />
            </a>
            <p className={styles.Tagline}>
              AI monitoring for Spring Boot in production.{" "}
              <em>Open-source, MCP-native, never your bottleneck.</em>
            </p>
            <div className={styles.Socials}>
              <a href={GITHUB_URL} aria-label="GitHub" target="_blank" rel="noopener noreferrer">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M9 19c-4.3 1.4 -4.3 -2.5 -6 -3m12 5v-3.5c0 -1 .1 -1.4 -.5 -2c2.8 -.3 5.5 -1.4 5.5 -6a4.6 4.6 0 0 0 -1.3 -3.2a4.2 4.2 0 0 0 -.1 -3.2s-1.1 -.3 -3.5 1.3a12.3 12.3 0 0 0 -6.2 0c-2.4 -1.6 -3.5 -1.3 -3.5 -1.3a4.2 4.2 0 0 0 -.1 3.2a4.6 4.6 0 0 0 -1.3 3.2c0 4.6 2.7 5.7 5.5 6c-.6 .6 -.6 1.2 -.5 2v3.5" />
                </svg>
              </a>
              <a href="https://x.com/axelixlabs" aria-label="X (Twitter)" target="_blank" rel="noopener noreferrer">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M4 4l11.733 16h4.267l-11.733 -16z" />
                  <path d="M4 20l6.768 -6.768m2.46 -2.46l6.772 -6.772" />
                </svg>
              </a>
              <a href="mailto:hello@axelix.io" aria-label="Email">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M3 7a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-10z" />
                  <path d="M3 7l9 6l9 -6" />
                </svg>
              </a>
            </div>
          </div>
          <div className={styles.NavCols}>
            <div className={styles.Col}>
              <h4>Product</h4>
              <ul>
                <li><a href="https://axelix.io/#capabilities">Capabilities</a></li>
                <li><a href="https://axelix.io/#install">Install</a></li>
                <li><a href="https://axelix.io/#faq">FAQ</a></li>
              </ul>
            </div>
            <div className={styles.Col}>
              <h4>Resources</h4>
              <ul>
                <li><a href="https://axelix.io/docs">Documentation</a></li>
                <li><a href={GITHUB_URL}>GitHub</a></li>
                <li><a href="https://axelix.io/docs">Changelog</a></li>
              </ul>
            </div>
            <div className={styles.Col}>
              <h4>Company</h4>
              <ul>
                <li><a href="https://axelix.io">About</a></li>
                <li><a href="/">Blog</a></li>
                <li><a href="mailto:hello@axelix.io">Contact</a></li>
              </ul>
            </div>
            <div className={styles.Col}>
              <h4>Legal</h4>
              <ul>
                <li><a href={`${GITHUB_URL}/blob/master/LICENSE`}>License</a></li>
                <li><a href="https://axelix.io">Privacy</a></li>
                <li><a href="https://axelix.io">Terms</a></li>
              </ul>
            </div>
          </div>
        </div>
        <div className={styles.FooterWatermark} aria-hidden="true">
          <span>AXELIX</span>
        </div>
        <div className={styles.FooterBottom}>
          <span className="copy">© 2026 Axelix Labs</span>
          <div className={styles.Meta}>
            <span>LGPL-3.0</span>
            <span className={styles.DotSep} />
            <a href="https://axelix.io" className={styles.Status}>
              All systems operational
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
};
