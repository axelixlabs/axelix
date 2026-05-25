import {
    ComplianceIcon,
    FleetIcon,
    OnPremIcon,
    RbacIcon,
    SsoIcon,
    SupportIcon,
} from "@/assets";

import styles from "./styles.module.css"

const EXTENSIONS = [
    {
        title: "SSO & identity",
        description: "SAML / OIDC, SCIM provisioning, group → role mapping. Plug into your existing IdP.",
        icon: <SsoIcon />,
    },
    {
        title: "Granular RBAC",
        description: "Per-action policies, per-service scopes, per-environment guards. One model for humans and agents.",
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
        description: "Offline install bundle, no telemetry, BYO container registry. For everything that can't leave the network.",
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
                    <div className={styles.IconWrapper}>
                        {icon}
                    </div>
                    <h3 className={styles.CardTitle}>{title}</h3>
                    <p className={styles.CardDescription}>{description}</p>
                </article>
            ))}
        </div>
    )
} 