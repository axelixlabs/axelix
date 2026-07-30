/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import { EmailIcon, ExternalLinkIcon, GitHubIcon, LinkedinIcon, LogoIcon, XTwitterIcon } from "@/assets";
import { BLOG_URL, DOCS_URL, GITHUB_URL, LINKEDIN_URL, MAIL_URL, OSS_LICENSE_URL, X_URL } from "@/lib/blog-metadata";

import styles from "./styles.module.css";

export const Footer = () => {
    return (
        <>
            <footer className={styles.Footer}>
                <div className={`wrap ${styles.Wrap}`}>
                    <div className={styles.Top}>
                        <div className={styles.BrandSide}>
                            <LogoIcon color="#fff" />
                            <p className={styles.Tag}>
                                AI monitoring for Spring Boot in production.{" "}
                                <em>Open-source, MCP-native, never your bottleneck.</em>
                            </p>
                            <div className={styles.Socials}>
                                <a href={GITHUB_URL} target="_blank" rel="noopener noreferrer">
                                    <GitHubIcon />
                                </a>
                                <a href={X_URL} target="_blank" rel="noopener noreferrer">
                                    <XTwitterIcon />
                                </a>
                                <a href={LINKEDIN_URL} target="_blank" rel="noopener noreferrer">
                                    <LinkedinIcon />
                                </a>
                                <a href={`mailto:${MAIL_URL}`}>
                                    <EmailIcon />
                                </a>
                            </div>
                        </div>

                        {/* Nav columns */}
                        <div className={styles.NavCols}>
                            <div className={styles.Col}>
                                <h4>Product</h4>
                                <ul>
                                    <li>
                                        <a href="#capabilities">Capabilities</a>
                                    </li>
                                    <li>
                                        <a href="#install">Install</a>
                                    </li>
                                    <li>
                                        <a href="#faq">FAQ</a>
                                    </li>
                                </ul>
                            </div>
                            <div className={styles.Col}>
                                <h4>Resources</h4>
                                <ul>
                                    <li>
                                        <a href={DOCS_URL} target="_blank" rel="noopener noreferrer">
                                            Documentation <ExternalLinkIcon className={styles.ExtArr} />
                                        </a>
                                    </li>

                                    <li>
                                        <a href={GITHUB_URL} target="_blank" rel="noopener noreferrer">
                                            GitHub <ExternalLinkIcon className={styles.ExtArr} />
                                        </a>
                                    </li>
                                    <li>
                                        <a href={`${GITHUB_URL}/releases`} target="_blank" rel="noopener noreferrer">
                                            Changelog <ExternalLinkIcon className={styles.ExtArr} />
                                        </a>
                                    </li>
                                </ul>
                            </div>
                            <div className={styles.Col}>
                                <h4>Company</h4>
                                <ul>
                                    <li>
                                        <a href="#" target="_blank" rel="noopener noreferrer">
                                            About
                                        </a>
                                    </li>
                                    <li>
                                        <a href={BLOG_URL} target="_blank" rel="noopener noreferrer">
                                            Blog <ExternalLinkIcon className={styles.ExtArr} />
                                        </a>
                                    </li>
                                    <li>
                                        <a href={`mailto:${MAIL_URL}`}>Contact</a>
                                    </li>
                                </ul>
                            </div>
                            <div className={styles.Col}>
                                <h4>Legal</h4>
                                <ul>
                                    <li>
                                        <a href={OSS_LICENSE_URL} target="_blank" rel="noopener noreferrer">
                                            OSS License
                                        </a>
                                    </li>
                                    <li>
                                        <a href="#" target="_blank" rel="noopener noreferrer">
                                            Privacy
                                        </a>
                                    </li>
                                    <li>
                                        <a href="#" target="_blank" rel="noopener noreferrer">
                                            Terms
                                        </a>
                                    </li>
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
                            <span>LGPL-3.0</span>
                            <span className={styles.DotSep} />
                            <a href="https://axelix.io" className={styles.Status}>
                                All systems operational
                            </a>
                        </div>
                    </div>
                </div>
            </footer>
        </>
    );
};
