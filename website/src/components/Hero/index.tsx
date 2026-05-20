import {  SparklesIcon } from "@/assets";
import styles from "./styles.module.css";
import { HeroBackground } from "./HeroBackground";

export const Hero = () => {
  return (
    <header className={styles.Hero}>
      <HeroBackground />
      <div className={`wrap ${styles.Inner}`}>
        <div>
          <h1 className={styles.H1}>
            Control your <span className={styles.Green}>Spring&nbsp;Boot</span>{" "}
            in <span className={styles.Deep}>production —</span>{" "}
            <span className={styles.Stroke}>not the other way around.</span>
          </h1>

          <p className={styles.Lede}>
            Axelix is the open-source console for{" "}
            <em>
              debugging, observing and operating mission-critical Spring Boot
              microservices.
            </em>{" "}
            Every capability is exposed twice — to engineers through the web,
            and to AI agents through an embedded MCP server.
          </p>

          <div className={styles.Actions}>
            <a
              href="#install"
              className={`${styles.Btn} ${styles.BtnGreen} ${styles.BtnLg}`}
            >
              Get started free <span className={styles.Arrow}>→</span>
            </a>
            <a href="#enterprise" className={`${styles.Btn} ${styles.BtnLg}`}>
              <SparklesIcon width="16" height="16" />
              Explore Enterprise
              <span className={styles.BadgeSoon}>In development</span>
            </a>
          </div>

          <div className={styles.Trust}>
            <div>
              <div className={styles.K}>Built for</div>
              <div className={styles.V}>
                Production
                <span className={styles.Small}>not test stands</span>
              </div>
            </div>
            <div>
              <div className={styles.K}>Exposed to</div>
              <div className={styles.V}>
                Humans &amp; agents
                <span className={styles.Small}>same RBAC, same audit</span>
              </div>
            </div>
            <div>
              <div className={styles.K}>Install</div>
              <div className={styles.V}>
                Fast setup<span className={styles.Small}>docker · helm</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}