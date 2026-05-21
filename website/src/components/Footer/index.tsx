import {
  DiscordIcon,
  EmailIcon,
  ExternalLinkIcon,
  GithubIcon,
  LinkedinIcon,
  XTwitterIcon,
} from "@/assets";
import { Logo } from "../Logo";
import styles from "./styles.module.css";

export const Footer = () => {
  return (
    <footer className={styles.Footer}>
      <div className={`wrap ${styles.Wrap}`}>
        <div className={styles.Top}>
          {/* Brand lockup */}
          <div className={styles.BrandSide}>
            <Logo />
            <p className={styles.Tag}>
              AI monitoring for Spring Boot in production.{" "}
              <em>Open-source, MCP-native, never your bottleneck.</em>
            </p>
            <div className={styles.Socials}>
              <a href="https://github.com/axelixlabs/axelix" target="_blank" rel="noopener noreferrer">
                <GithubIcon />
              </a>
              <a href="#" target="_blank" rel="noopener noreferrer">
                <XTwitterIcon />
              </a>
              <a href="#" target="_blank" rel="noopener noreferrer">
                <LinkedinIcon />
              </a>
              <a href="#" target="_blank" rel="noopener noreferrer">
                <DiscordIcon />
              </a>
              <a href="mailto:hello@axelix.io">
                <EmailIcon />
              </a>
            </div>
          </div>

          {/* Nav columns */}
          <div className={styles.NavCols}>
            <div className={styles.Col}>
              <h4>Product</h4>
              <ul>
                <li><a href="#capabilities">Capabilities</a></li>
                <li><a href="#install">Install</a></li>
                <li><a href="#enterprise">Enterprise</a></li>
                <li><a href="#faq">FAQ</a></li>
              </ul>
            </div>
            <div className={styles.Col}>
              <h4>Resources</h4>
              <ul>
                <li>
                  <a href="https://axelix.io/docs/product/introduction" target="_blank" rel="noopener noreferrer">
                    Documentation <ExternalLinkIcon className={styles.ExtArr} />
                  </a>
                </li>
                <li>
                  <a href="https://github.com/axelixlabs/axelix" target="_blank" rel="noopener noreferrer">
                    GitHub <ExternalLinkIcon className={styles.ExtArr} />
                  </a>
                </li>
                <li><a href="#" target="_blank" rel="noopener noreferrer">MCP catalog</a></li>
                <li><a href="#" target="_blank" rel="noopener noreferrer">Changelog</a></li>
              </ul>
            </div>
            <div className={styles.Col}>
              <h4>Company</h4>
              <ul>
                <li><a href="#" target="_blank" rel="noopener noreferrer">About</a></li>
                <li><a href="https://axelix.io/blog" target="_blank" rel="noopener noreferrer">Blog</a></li>
                <li><a href="#" target="_blank" rel="noopener noreferrer">Careers</a></li>
                <li><a href="mailto:hello@axelix.io" target="_blank" rel="noopener noreferrer">Contact</a></li>
              </ul>
            </div>
            <div className={styles.Col}>
              <h4>Legal</h4>
              <ul>
                <li><a href="https://github.com/axelixlabs/axelix?tab=LGPL-3.0-1-ov-file" target="_blank" rel="noopener noreferrer">LGPL-3.0</a></li>
                <li><a href="#" target="_blank" rel="noopener noreferrer">Privacy</a></li>
                <li><a href="#" target="_blank" rel="noopener noreferrer">Terms</a></li>
                <li><a href="https://axelix.io/docs/setting-up-master-ui/authentication" target="_blank" rel="noopener noreferrer">Security</a></li>
              </ul>
            </div>
          </div>
        </div>

        {/* Watermark */}
        <div className={styles.Watermark}>
          <span>AXELIX</span>
        </div>

        {/* Bottom strip */}
        <div className={styles.Bottom}>
          <span>© 2026 Axelix Labs</span>
          <div className={styles.Meta}>
            <span>v1.0.0</span>
            <span className={styles.DotSep}></span>
            <span>LGPL-3.0</span>
            <span className={styles.DotSep}></span>
            <span className={styles.Status}>All systems operational</span>
          </div>
        </div>
      </div>
    </footer>
  );
};
