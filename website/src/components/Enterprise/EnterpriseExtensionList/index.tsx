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
import { ComplianceIcon, FleetIcon, OnPremIcon, RbacIcon, SsoIcon, SupportIcon } from "@/assets";

import styles from "./styles.module.css";

const EXTENSIONS = [
    {
        title: "SSO & identity",
        description: "SAML / OIDC, SCIM provisioning, group → role mapping. Plug into your existing IdP.",
        icon: <SsoIcon />,
    },
    {
        title: "Granular RBAC",
        description:
            "Per-action policies, per-service scopes, per-environment guards. One model for humans and agents.",
        icon: <RbacIcon />,
    },
    {
        title: "Compliance & audit",
        description: "Immutable audit log, SOC 2 / ISO 27001 evidence pack, BYO-S3 export, retention policies.",
        icon: <ComplianceIcon />,
    },
    {
        title: "Multi-cluster fleet",
        description: "Federated masters across regions, single pane of glass, region-aware routing.",
        icon: <FleetIcon />,
    },
    {
        title: "On-prem & air-gapped",
        description:
            "Offline install bundle, no telemetry, BYO container registry. For everything that can't leave the network.",
        icon: <OnPremIcon />,
    },
    {
        title: "Dedicated support",
        description: "Named engineer, SLA, priority CVE patches, roadmap input. A vendor at the end of an email.",
        icon: <SupportIcon />,
    },
];

export const EnterpriseExtensionList = () => {
    return (
        <div className={styles.MainWrapper}>
            {EXTENSIONS.map(({ title, icon, description }) => (
                <article key={title} className={styles.Card}>
                    <div className={styles.IconWrapper}>{icon}</div>
                    <h3 className={styles.CardTitle}>{title}</h3>
                    <p className={styles.CardDescription}>{description}</p>
                </article>
            ))}
        </div>
    );
};
