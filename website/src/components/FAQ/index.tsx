"use client";

import { PlusIcon } from "@/assets";
import { useState, type ReactNode } from "react";
import styles from "./styles.module.css";

type Item = { q: string; a: ReactNode };

const ITEMS: Item[] = [
  {
    q: "What is Axelix OSS?",
    a: (
      <p>
        The open-source console for debugging, observing and operating Spring
        Boot microservices in production. <em>Every capability is exposed
        twice</em> — to engineers through a web console, and to AI agents
        through an embedded MCP server. A single role model gates both.
      </p>
    ),
  },
  {
    q: "Which Spring Boot versions are supported?",
    a: (
      <p>
        Spring Boot 2, 3 and 4 — through dedicated starter artifacts (
        <code>axelix-spring-boot-2-starter</code>, <code>-3-starter</code>,{" "}
        <code>-4-starter</code>). The master runs on JVM 11–25.{" "}
        <em>No JVM agent, no flags, no custom boot order.</em>
      </p>
    ),
  },
  {
    q: "Is it really free?",
    a: (
      <p>
        Yes. Axelix OSS is licensed under <em>LGPL-3.0</em> — link it into
        your production apps, fork it, ship your own changes. We&apos;re
        building <em>Axelix Enterprise</em> on top (extensions and paid
        support for teams that want them), but the open core stays open.
      </p>
    ),
  },
  {
    q: "Is it safe to run in production?",
    a: (
      <p>
        It&apos;s designed for production. The master speaks to your services
        over an authenticated channel; a single role model gates both human
        engineers and AI agents — each identity sees only the data and actions
        its role permits. Destructive operations are explicit and confirmable.
      </p>
    ),
  },
  {
    q: "How is it different from Micrometer / Actuator?",
    a: (
      <p>
        Actuator exposes the data; <em>Axelix exposes the verbs.</em> We build
        on the actuator endpoints your apps already have, then add what
        actuator doesn&apos;t: a unified fleet console, an MCP server, runtime
        mutation under a role model, transactional inspection with SQL
        timelines, and a clear story for AI agents.
      </p>
    ),
  },
  {
    q: "What is Axelix Enterprise?",
    a: (
      <p>
        We&apos;re building something bigger. <em>Stay tuned.</em>
      </p>
    ),
  },
];

export const Faq = () => {
  const [open, setOpen] = useState<number | null>(null);

  return (
    <section className={styles.Faq} id="faq">
      <div className={`wrap ${styles.Wrap}`}>
        <div className={styles.Header}>
          <div>
            <span className={styles.Eyebrow}>FAQ</span>
            <h2 className={styles.H2}>
              Questions, <span className={styles.Stroke}>answered.</span>
            </h2>
          </div>
          <p className={styles.Intro}>
            Everything you&apos;d ask before pointing a console at your fleet
            — license, safety, platform support, and what&apos;s next.
          </p>
        </div>

        <ol className={styles.List}>
          {ITEMS.map((it, i) => {
            const isOpen = open === i;
            return (
              <li
                key={i}
                className={`${styles.Item} ${isOpen ? styles.Open : ""}`}
              >
                <button
                  className={styles.Trigger}
                  type="button"
                  onClick={() => setOpen(isOpen ? null : i)}
                >
                  <span className={styles.Q}>{it.q}</span>
                  <span className={styles.Icon}>
                    <PlusIcon />
                  </span>
                </button>
                <div className={styles.Answer}>
                  <div className={styles.Inner}>{it.a}</div>
                </div>
              </li>
            );
          })}
        </ol>

        <div className={styles.ContactCard}>
          <div>
            <span className={styles.Label}>Still curious?</span>
            <h3>Reach the team — we reply within a working day.</h3>
            <p>
              Architectural questions, production-readiness checks, enterprise
              pilots. Anything not answered above is the kind of thing we like
              answering directly.
            </p>
          </div>
          <a href="mailto:hello@axelix.io" className={styles.Cta}>
            hello@axelix.io <span className={styles.Arr}>→</span>
          </a>
        </div>
      </div>
    </section>
  );
}
