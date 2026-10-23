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
import { SectionLabel } from "../../SectionLabel";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

const FEATURES = [
    {
        title: "Enforcement policies",
        description:
            "Policies as code for code quality and dependency health. A service that breaks a policy does not reach production.",
    },
    {
        title: "SSO & identity",
        description: "SAML / OIDC, SCIM provisioning, group → role mapping. Plug into your existing IdP.",
    },
    {
        title: "Granular RBAC",
        description:
            "Per-action policies, per-service scopes, per-environment guards. One model for humans and agents.",
    },
    {
        title: "Compliance & audit",
        description: "Immutable audit log, SOC 2 / ISO 27001 evidence pack, BYO-S3 export, retention policies.",
    },
    {
        title: "Multi-cluster fleet",
        description: "Federated masters across regions, single pane of glass, region-aware routing.",
    },
    {
        title: "On-prem & air-gapped",
        description:
            "Offline install bundle, no telemetry, BYO container registry. For everything that can't leave the network.",
    },
];

const SUPPORT = [
    { title: "Named engineer", description: "One contact who knows your fleet." },
    { title: "SLA", description: "24/7 response commitments." },
    { title: "Priority CVE patches", description: "Fixes for your version first." },
    { title: "Roadmap input", description: "Shape what ships next." },
];

export const Includes = () => {
    return (
        <section className={styles.MainWrapper} id="includes">
            <div className="MainContainer">
                <SectionLabel index="03" label="What Enterprise includes" />
                <h2 className={`${shared.Title} ${styles.Title}`}>Features and support, in one contract</h2>

                <div className={styles.Features}>
                    {FEATURES.map(({ title, description }) => (
                        <div key={title} className={styles.Feature}>
                            <h3 className={styles.FeatureTitle}>{title}</h3>
                            <p className={styles.FeatureDescription}>{description}</p>
                        </div>
                    ))}
                </div>

                <div className={styles.Support} id="support">
                    <div>
                        <div className={shared.Eyebrow}>Included with Enterprise</div>
                        <h2 className={styles.SupportTitle}>A vendor at the end of an email.</h2>
                    </div>
                    <div className={styles.SupportItems}>
                        {SUPPORT.map(({ title, description }) => (
                            <div key={title} className={styles.SupportItem}>
                                <span className={styles.SupportItemTitle}>{title}</span>
                                <span className={styles.SupportItemDescription}>{description}</span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </section>
    );
};
