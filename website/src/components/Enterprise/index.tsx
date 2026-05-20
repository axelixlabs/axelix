import {
  ComplianceIcon,
  FleetIcon,
  OnPremIcon,
  RbacIcon,
  SsoIcon,
  SupportIcon,
} from "@/assets";
import styles from "./styles.module.css";

const EXTENSIONS = [
  {
    title: "SSO & identity",
    desc: "SAML / OIDC, SCIM provisioning, group → role mapping. Plug into your existing IdP.",
    icon: <SsoIcon />,
  },
  {
    title: "Granular RBAC",
    desc: "Per-action policies, per-service scopes, per-environment guards. One model for humans and agents.",
    icon: <RbacIcon />,
  },
  {
    title: "Compliance & audit",
    desc: "Immutable audit log, SOC 2 / ISO 27001 evidence pack, BYO-S3 export, retention policies.",
    icon: <ComplianceIcon />,
  },
  {
    title: "Multi-cluster fleet",
    desc: "Federated masters across regions, single pane of glass, region-aware routing.",
    icon: <FleetIcon />,
  },
  {
    title: "On-prem & air-gapped",
    desc: "Offline install bundle, no telemetry, BYO container registry. For everything that can't leave the network.",
    icon: <OnPremIcon />,
  },
  {
    title: "Dedicated support",
    desc: "Named engineer, SLA, priority CVE patches, roadmap input. A vendor at the end of an email.",
    icon: <SupportIcon />,
  },
];

export const Enterprise = () => {
  return (
    <section className={styles.Enterprise} id="enterprise">
      <div className={`wrap ${styles.Wrap}`}>
        <div className={styles.Header}>
          <div>
            <span className={styles.Eyebrow}>Enterprise · early access</span>
            <h2 className={styles.H2}>
              Open core stays open.{" "}
              <span className={styles.Stroke}>Enterprise lives on top.</span>
            </h2>
          </div>
          <p className={styles.Lead}>
            <em>Axelix Enterprise</em> extends the open core with the controls,
            integrations and support platform, security and compliance teams
            ask for — without forking the surface engineers already learned.
            The OSS keeps shipping, in the open. Enterprise is what you reach
            for when one cluster becomes ten and audit asks who changed what at
            3 AM.
          </p>
        </div>

        <div className={styles.Body}>
          {/* Left: layered architecture metaphor */}
          <div>
            <div className={styles.Stack}>
              <div className={`${styles.Layer} ${styles.Top}`}>
                <div className={styles.Lhs}>
                  <span className={styles.Lbl}>Tier 3 · Support</span>
                  <span className={styles.Name}>Dedicated support &amp; SLA</span>
                  <span className={styles.Meta}>
                    24/7 · named engineer · priority CVE
                  </span>
                </div>
                <span
                  className={styles.Badge}
                  style={{
                    background: "transparent",
                    color: "var(--axelix-green-400)",
                    border:
                      "1px solid color-mix(in srgb, var(--axelix-green-400) 30%, transparent)",
                  }}
                >
                  contract
                </span>
              </div>
              <div className={`${styles.Layer} ${styles.Mid}`}>
                <div className={styles.Lhs}>
                  <span className={styles.Lbl}>Tier 2 · Extensions</span>
                  <span className={styles.Name}>Enterprise extensions</span>
                  <span className={styles.Meta}>
                    SSO · RBAC · audit · multi-cluster · on-prem
                  </span>
                </div>
                <span
                  className={styles.Badge}
                  style={{
                    background: "transparent",
                    color: "var(--axelix-green-400)",
                    border:
                      "1px solid color-mix(in srgb, var(--axelix-green-400) 30%, transparent)",
                  }}
                >
                  licensed
                </span>
              </div>
              <div className={`${styles.Layer} ${styles.Base}`}>
                <div className={styles.Lhs}>
                  <span className={styles.Lbl}>Tier 1 · Foundation</span>
                  <span className={styles.Name}>Axelix OSS</span>
                  <span className={styles.Meta}>
                    LGPL-3.0 · forever free · the same console
                  </span>
                </div>
                <span className={styles.Badge}>always open</span>
              </div>
            </div>
            <div className={styles.StackFoot}>
              Same surface · same MCP · one product
            </div>
          </div>

          {/* Right: extensions list */}
          <div className={styles.ExtGrid}>
            {EXTENSIONS.map((e) => (
              <article key={e.title} className={styles.Ext}>
                <div className={styles.Ic}>
                  {e.icon}
                </div>
                <h3>{e.title}</h3>
                <p>{e.desc}</p>
              </article>
            ))}
          </div>
        </div>

        {/* Early-access CTA */}
        <div className={styles.Cta}>
          <div>
            <span className={styles.EaEyebrow}>Early access · Q3 2026</span>
            <h3>Want a seat in the first pilots?</h3>
            <p>
              We&apos;re picking a handful of teams to shape what ships first.
              Tell us about your fleet — we&apos;ll be in touch within a
              working day.
            </p>
          </div>
          <a href="mailto:enterprise@axelix.io" className={styles.BtnFill}>
            enterprise@axelix.io <span className={styles.Arr}>→</span>
          </a>
        </div>
      </div>
    </section>
  );
};
