import {
  BeansIcon,
  CachesIcon,
  ConditionsIcon,
  ConfigIcon,
  EnvironmentIcon,
  ExportIcon,
  GcIcon,
  InstanceIcon,
  LoggersIcon,
  TasksIcon,
  ComposeIcon,
  TransactionsIcon,
  VisGraphIcon
} from "@/assets";
import styles from "./styles.module.css";

function DualPill() {
  return (
    <span
      className={styles.Dual}
      title="Exposed to the web console and to MCP agents"
    >
      <span className={`${styles.D} ${styles.W}`}>Web</span>
      <span className={styles.Sep}>·</span>
      <span className={`${styles.D} ${styles.M}`}>MCP</span>
    </span>
  );
}

function CapabilityLink({
  href,
  children,
}: {
  href: string;
  children: React.ReactNode;
}) {
  return (
    <a
      className={styles.TileLink}
      href={href}
      target="_blank"
      rel="noopener noreferrer"
    >
      {children}
    </a>
  );
}

export const Capabilities = () => {
  return (
    <section className={styles.Caps} id="capabilities">
      <div className={`wrap ${styles.Wrap}`}>
        <div className={styles.Header}>
          <div>
            <span className={styles.Eyebrow}>Capabilities</span>
            <h2 className={styles.H2}>
              Verbs for the JVM — same surface for{" "}
              <span className={styles.Accent}>people</span> and{" "}
              <span className={styles.Blue}>agents</span>.
            </h2>
          </div>
          <p className={styles.Intro}>
            Every capability is exposed twice. <em>Engineers</em> reach it
            through a web console; <em>AI agents</em> reach the same actions
            through an embedded MCP server. A single role model gates both —
            each identity, human or agent, sees only the data and actions its
            role permits.
          </p>
        </div>

        {/* Featured 4 */}
        <div className={styles.Featured}>
          {/* Transactions */}
          <CapabilityLink href="https://axelix.io/docs/features/transaction-control">
            <article className={`${styles.TileC} ${styles.Feat}`}>
              <DualPill />
              <div className={styles.Icon}>
                <TransactionsIcon width="22" height="22" />
              </div>
              <div>
                <h3>Transactional inspection</h3>
                <p>
                  Live transactions, durations, the SQL timeline of each one —
                  the verbs your APM forgot.
                </p>
              </div>
              <div className={styles.Vis}>
                <div className={styles.VisBars}>
                  <span style={{ height: "30%" }}></span>
                  <span style={{ height: "55%" }}></span>
                  <span style={{ height: "40%" }}></span>
                  <span className={styles.Hi} style={{ height: "92%" }}></span>
                  <span style={{ height: "48%" }}></span>
                  <span style={{ height: "62%" }}></span>
                  <span style={{ height: "38%" }}></span>
                  <span style={{ height: "70%" }}></span>
                  <span style={{ height: "44%" }}></span>
                  <span className={styles.Hi} style={{ height: "84%" }}></span>
                  <span style={{ height: "50%" }}></span>
                  <span className={styles.Lo} style={{ height: "22%" }}></span>
                </div>
              </div>
            </article>
          </CapabilityLink>

          {/* Configuration properties */}
          <CapabilityLink href="https://axelix.io/docs/features/configuration-properties">
            <article className={`${styles.TileC} ${styles.Feat}`}>
              <DualPill />
              <div className={styles.Icon}>
                <ConfigIcon width="22" height="22" />
              </div>
              <div>
                <h3>Configuration properties</h3>
                <p>
                  Effective values for every{" "}
                  <code className={styles.CodeInline}>@Value</code> and bound
                  prefix — and where they actually came from.
                </p>
              </div>
              <div className={styles.Vis}>
                <div className={styles.VisKv}>
                  <div className={styles.R}>
                    <span>datasource.url</span>
                    <span className={styles.Val}>vault</span>
                  </div>
                  <div className={styles.R}>
                    <span>cache.ttl</span>
                    <span className={styles.Val}>30s</span>
                  </div>
                  <div className={styles.R}>
                    <span>retry.max</span>
                    <span className={styles.Val}>env</span>
                  </div>
                  <div className={styles.R}>
                    <span>feature.x</span>
                    <span className={`${styles.Val} ${styles.Blue}`}>on</span>
                  </div>
                </div>
              </div>
            </article>
          </CapabilityLink>

          {/* Loggers */}
          <CapabilityLink href="https://axelix.io/docs/features/loggers">
            <article className={`${styles.TileC} ${styles.Feat}`}>
              <DualPill />
              <div className={styles.Icon}>
                <LoggersIcon width="22" height="22" />
              </div>
              <div>
                <h3>Loggers</h3>
                <p>
                  Flip log levels per package, live — no redeploy, no SSH, fully
                  audited.
                </p>
              </div>
              <div className={styles.Vis}>
                <div className={styles.VisTree}>
                  <div className={styles.R}>
                    <span className={styles.Key}>org.hibernate.SQL</span>
                    <span className={`${styles.Lvl} ${styles.Trace}`}>trace</span>
                  </div>
                  <div className={styles.R}>
                    <span className={styles.Key}>o.s.web</span>
                    <span className={`${styles.Lvl} ${styles.Info}`}>info</span>
                  </div>
                  <div className={styles.R}>
                    <span className={styles.Key}>com.acme.svc</span>
                    <span className={`${styles.Lvl} ${styles.Debug}`}>debug</span>
                  </div>
                </div>
              </div>
            </article>
          </CapabilityLink>

          {/* Beans + Conditions */}
          <CapabilityLink href="https://axelix.io/docs/features/beans">
            <article className={`${styles.TileC} ${styles.Feat}`}>
              <DualPill />
              <div className={styles.Icon}>
                <BeansIcon width="22" height="22" />
              </div>
              <div>
                <h3>Beans &amp; conditions</h3>
                <p>
                  The live bean graph plus the{" "}
                  <code className={styles.CodeInline}>@Conditional</code>{" "}
                  verdicts — why each bean is here, or isn&apos;t.
                </p>
              </div>
              <div className={styles.Vis}>
                <VisGraphIcon className={styles.VisGraph} />
              </div>
            </article>
          </CapabilityLink>
        </div>

        {/* Compact 8 */}
        <div className={styles.Compact}>
          <CompactTile
            title="Environment"
            desc="Every env source the JVM sees, ranked by precedence — with selective masking."
            icon={<EnvironmentIcon width="16" height="16" />}
            href="https://axelix.io/docs/features/properties"
          />
          <CompactTile
            title="Caches"
            desc="Cache managers, hit/miss rates, evict and clear — from a button or a tool call."
            icon={<CachesIcon width="16" height="16" />}
            href="https://axelix.io/docs/features/caches"
          />
          <CompactTile
            title="Thread dump"
            desc="Threads, states, locks and contention — on demand, no kill-switch required."
            icon={<ComposeIcon width="16" height="16" />}
            href="https://axelix.io/docs/features/thread-dump"
          />
          <CompactTile
            title="Garbage collector"
            desc="Live GC events, pause times and tuning signals — not just a chart, a feed."
            icon={<GcIcon width="16" height="16" />}
            href="https://axelix.io/docs/features/garbage-collector"
          />
          <CompactTile
            title="Scheduled tasks"
            desc="Inspect cron and fixed-delay tasks; toggle, force-run, or rewrite expressions live."
            icon={<TasksIcon width="16" height="16" />}
            href="https://axelix.io/docs/features/scheduled-tasks"
          />
          <CompactTile
            title="Conditions"
            desc="Auto-configuration matched and not-matched, with the reason for every verdict."
            icon={<ConditionsIcon width="16" height="16" />}
            href="https://axelix.io/docs/features/conditions"
          />
          <CompactTile
            title="Instance details"
            desc="Service metadata, build info, runtime version — the boring stuff you need at 3 AM."
            icon={<InstanceIcon width="16" height="16" />}
            href="https://axelix.io/docs/features/details"
          />
          <CompactTile
            title="Diagnostic export"
            desc="One-click bundle of dumps, beans, configs and conditions — for incidents and audits."
            icon={<ExportIcon width="16" height="16" />}
            href="https://axelix.io/docs/features/details#download-state-components"
          />
        </div>

        <div className={styles.CloserLink}>
          <a href="https://axelix.io/docs/features/details" target="_blank" rel="noopener noreferrer">
            More verbs <span className={styles.Arr}>→</span> capability matrix
          </a>
        </div>
      </div>
    </section>
  );
};

function CompactTile({
  icon,
  title,
  desc,
  href,
}: {
  icon: React.ReactNode;
  title: string;
  desc: string;
  href: string;
}) {
  return (
    <CapabilityLink href={href}>
      <article className={`${styles.TileC} ${styles.Cmpt}`}>
        <DualPill />
        <div className={styles.Icon}>
          {icon}
        </div>
        <h4>{title}</h4>
        <p>{desc}</p>
      </article>
    </CapabilityLink>
  );
}
