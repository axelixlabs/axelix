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
import { ENTERPRISE_EMAIL } from "@/utils";

import { SectionLabel } from "../../SectionLabel";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

const ENTERPRISE_FEATURES = [
    "Quality policies",
    "Dependency policies",
    "SSO & SCIM",
    "Granular RBAC",
    "Immutable audit log",
    "Multi-cluster fleet",
    "Air-gapped install",
    "Support & SLA",
    "Priority CVE patches",
];

const OSS_FEATURES = [
    "Runtime inspection",
    "MCP server",
    "Live log levels",
    "Thread dumps & GC",
    "Quality findings",
    "Dependency status",
];

export const EnterpriseHero = () => {
    return (
        <section className={styles.MainWrapper}>
            <div className={`MainContainer ${styles.Inner}`}>
                <div>
                    <SectionLabel label="Enterprise · early access" dark />
                    <h1 className={styles.Title}>
                        Open core stays open. <span className={styles.Underlined}>Enterprise lives on top.</span>
                    </h1>
                    <p className={styles.Description}>
                        Open source lets you see what is wrong inside your Java services. <b>Axelix Enterprise</b> lets
                        you control it: policies that keep problematic code and unsupported dependencies out of
                        production, plus the identity, audit and support that security and compliance teams require.
                    </p>
                    <div className={styles.Actions}>
                        <a href={`mailto:${ENTERPRISE_EMAIL}`} className={shared.PrimaryButton}>
                            Join the pilot →
                        </a>
                        <a href="#editions" className={styles.SecondaryButton}>
                            Compare editions
                        </a>
                    </div>
                </div>

                <div>
                    <div className={`${styles.Tier} ${styles.EnterpriseTier}`}>
                        <div className={styles.TierHeader}>
                            <div className={styles.TierName}>
                                <span className={shared.MonoLabel}>Enterprise · control</span>
                                <span className={styles.TierTitle}>Axelix Enterprise</span>
                            </div>
                            <span className={`${shared.MonoLabel} ${styles.TierBadge}`}>contract</span>
                        </div>
                        <div className={styles.Chips}>
                            {ENTERPRISE_FEATURES.map((feature) => (
                                <span key={feature} className={styles.Chip}>
                                    {feature}
                                </span>
                            ))}
                        </div>
                    </div>
                    <div className={styles.Connector}>builds on, never forks</div>
                    <div className={`${styles.Tier} ${styles.OssTier}`}>
                        <div className={styles.TierHeader}>
                            <div className={styles.TierName}>
                                <span className={shared.MonoLabel}>Open source · see</span>
                                <span className={styles.TierTitle}>Axelix OSS</span>
                                <span className={styles.TierMeta}>LGPL-3.0 · forever free</span>
                            </div>
                            <span className={`${shared.MonoLabel} ${styles.TierBadge}`}>always open</span>
                        </div>
                        <div className={styles.Chips}>
                            {OSS_FEATURES.map((feature) => (
                                <span key={feature} className={styles.Chip}>
                                    {feature}
                                </span>
                            ))}
                        </div>
                    </div>
                    <div className={styles.StackFooter}>Same surface · same MCP · one product</div>
                </div>
            </div>
        </section>
    );
};
