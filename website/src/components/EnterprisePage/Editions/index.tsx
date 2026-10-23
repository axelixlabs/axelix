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
import { ENTERPRISE_EMAIL, GET_STARTED_HREF } from "@/utils";

import { SectionLabel } from "../../SectionLabel";
import shared from "../shared.module.css";

import styles from "./styles.module.css";

const OSS_POINTS = [
    "Live runtime inspection: beans, configuration, transactions, loggers, thread dumps, GC",
    "Embedded MCP server for AI agents",
    "Code quality findings for every service",
    "Maintenance status of every dependency",
    "Self-hosted, community support on GitHub",
];

const ENTERPRISE_POINTS = [
    "Quality and dependency policies enforced before production",
    "SSO, SCIM and granular RBAC for humans and agents",
    "Immutable audit log and compliance evidence",
    "Multi-cluster fleet, on-prem and air-gapped installs",
    "Named engineer, SLA and priority CVE patches",
];

// `oss: null` marks a capability the open-source edition does not have.
const COMPARISON = [
    { area: "Runtime inspection", oss: "✓ Console and MCP", enterprise: "✓ Console and MCP" },
    { area: "Live changes", oss: "✓ Log levels, caches, no redeploy", enterprise: "✓ Plus per-environment guards" },
    {
        area: "Code quality",
        oss: "See findings per service",
        enterprise: "Enforce policies, block deploys that fail them",
    },
    {
        area: "Dependencies",
        oss: "See maintenance status",
        enterprise:
            "Policies by service type, e.g. no discontinued or maintenance-mode libraries in mission-critical services",
    },
    {
        area: "Access control",
        oss: "One role model for humans and agents",
        enterprise: "Per-action, per-service, per-environment policies",
    },
    { area: "SSO & identity", oss: null, enterprise: "SAML / OIDC, SCIM, group → role mapping" },
    {
        area: "Audit & compliance",
        oss: null,
        enterprise: "Immutable audit log, SOC 2 / ISO 27001 evidence pack, BYO-S3 export",
    },
    { area: "Fleet", oss: "Single cluster", enterprise: "Federated masters across regions" },
    { area: "Deployment", oss: "Self-hosted", enterprise: "Plus offline bundle, air-gapped, no telemetry" },
    { area: "Support", oss: "Community, GitHub", enterprise: "Named engineer, SLA, priority CVE patches" },
    { area: "License", oss: "LGPL-3.0", enterprise: "Commercial contract" },
];

export const Editions = () => {
    return (
        <section className={styles.MainWrapper} id="editions">
            <div className="MainContainer">
                <SectionLabel index="02" label="Editions" />
                <h2 className={`${shared.Title} ${styles.Title}`}>
                    Open source shows you the problems. <br />
                    Enterprise controls them.
                </h2>

                <div className={styles.Cards}>
                    <div className={`${styles.Card} ${styles.OssCard}`}>
                        <div className={styles.CardHeader}>
                            <span className={shared.MonoLabel}>Open source</span>
                            <span className={`${shared.MonoLabel} ${styles.CardBadge}`}>LGPL-3.0</span>
                        </div>
                        <div>
                            <div className={styles.CardTitle}>Axelix OSS</div>
                            <div className={styles.CardSubtitle}>
                                Free, forever. For teams who want to see inside every service.
                            </div>
                        </div>
                        <ul className={styles.Points}>
                            {OSS_POINTS.map((point) => (
                                <li key={point}>{point}</li>
                            ))}
                        </ul>
                        <a href={GET_STARTED_HREF} className={styles.OutlineButton}>
                            Get started free
                        </a>
                    </div>

                    <div className={`${styles.Card} ${styles.EnterpriseCard}`}>
                        <div className={styles.CardHeader}>
                            <span className={shared.MonoLabel}>Enterprise</span>
                            <span className={`${shared.MonoLabel} ${styles.CardBadge}`}>Contract</span>
                        </div>
                        <div>
                            <div className={styles.CardTitle}>Axelix Enterprise</div>
                            <div className={styles.CardSubtitle}>
                                Everything in OSS, plus control over what reaches production and a support contract.
                            </div>
                        </div>
                        <ul className={styles.Points}>
                            {ENTERPRISE_POINTS.map((point) => (
                                <li key={point}>{point}</li>
                            ))}
                        </ul>
                        <a
                            href={`mailto:${ENTERPRISE_EMAIL}`}
                            className={`${shared.PrimaryButton} ${styles.CardButton}`}
                        >
                            Talk to us about Enterprise
                        </a>
                    </div>
                </div>

                <div className={styles.Comparison}>
                    <h3 className={styles.ComparisonTitle}>Side by side</h3>
                    <div className={styles.Table} role="table">
                        <div className={`${styles.Row} ${styles.HeaderRow}`} role="row">
                            <span role="columnheader" />
                            <span role="columnheader">Axelix OSS</span>
                            <span role="columnheader" className={styles.EnterpriseHeader}>
                                Axelix Enterprise
                            </span>
                        </div>
                        {COMPARISON.map(({ area, oss, enterprise }) => (
                            <div key={area} className={styles.Row} role="row">
                                <span role="rowheader" className={styles.Area}>
                                    {area}
                                </span>
                                <span role="cell" className={oss ? "" : styles.Missing}>
                                    {oss ?? "—"}
                                </span>
                                <span role="cell">{enterprise}</span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </section>
    );
};
