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
"use client";
import { PlusIcon } from "@/assets";

import { ReactNode, useState } from "react";

import styles from "./styles.module.css";

interface IFAQItem {
    question: string;
    answer: ReactNode;
}

const ITEMS: IFAQItem[] = [
    {
        question: "What is Axelix OSS?",
        answer: (
            <p>
                The open-source solution for making sure your Java Spring Boot services are efficient, secure and
                memory-wise. Axelix OSS is the core of the product, which is Open Source and can be found on GitHub.
            </p>
        ),
    },
    {
        question: "What is Axelix Enterprise? What is the difference between OSS and Enterprise?",
        answer: (
            <p>
                First of all, Axelix is an <strong>Open Core</strong> product. Our Axelix OSS core is free of charge to
                use for anybody. It is available on{" "}
                <a className={styles.ClickableLink} href="https://github.com/axelixlabs/axelix">
                    GitHub
                </a>{" "}
                under LGPL. Axelix OSS is designed to assist the users to <strong>locate</strong> problems. <br />
                <br /> Axelix Enterprise adds a level on top, where it allows to{" "}
                <strong>configure policies-as-a-code</strong> to make sure your deployments stay efficient and
                performant.
            </p>
        ),
    },
    {
        question: "How does it work on the top-level?",
        answer: (
            <p>
                On the high level, it works by starter gathering certain information about your application either at
                boot time, or at runtime. This information is then gathered in the Axelix Master, which stores it in its
                database.
                <br />
                <br />
                <strong>We care about your deployments</strong>. Thus we specifically maintain a set of benchmarks to
                make sure we&#39;re not introducing any runtime/performance penalties for living applications.
            </p>
        ),
    },
    {
        question: "What about Access Control?",
        answer: (
            <p>
                <strong>Axelix has built-in RBAC</strong>. For small teams, it allows to create & store users in its own
                local database (Axelix Master - the central Axelix component - has the database). For large teams,
                Axelix supports OIDC and OAuth2 for SSO -{" "}
                <strong>this is all available in Axelix OSS, it is free of charge</strong>.
            </p>
        ),
    },
    {
        question: "Which Spring Boot versions are supported?",
        answer: (
            <p>
                Currently we support Spring Boot 2, 3 and 4 through dedicated starter artifacts (
                <code>axelix-spring-boot-2-starter</code>, <code>-3-starter</code>, <code>-4-starter</code>) and a
                specific build plugin (for Maven or Gradle, it is the same for all Spring Boot versions). The master
                Java 25. <em>No JVM agent, no flags, no custom boot order.</em>
            </p>
        ),
    },
    {
        question: "Does it only work with Spring Boot?",
        answer: (
            <p>
                As of now - yes. That said, we <strong>have</strong> plans to extend the support so that deployments
                that does not use Spring Boot could benefit from Axelix as well. That may include Quarkus/Micronaut
                services, or services that have their own home-grown framework.
            </p>
        ),
    },
    {
        question: "Is it safe to run in production?",
        answer: (
            <p>
                <strong>It&apos;s designed to run in production</strong>. The master speaks to your services over an
                authenticated channel; a single role model gates both human engineers and AI agents - each identity sees
                only the data and actions its role permits. Destructive operations are explicit and confirmable.
            </p>
        ),
    },
];

export const FAQList = () => {
    const [open, setOpen] = useState<number | null>(null);

    return (
        <ol className={styles.MainWrapper}>
            {ITEMS.map(({ question, answer }, index) => {
                const isOpen = open === index;

                return (
                    <li key={question} className={`${styles.Accordion} ${isOpen ? styles.Open : ""}`}>
                        <button
                            className={styles.WrapperButton}
                            type="button"
                            onClick={() => setOpen(isOpen ? null : index)}
                        >
                            <span className={styles.Question}>{question}</span>
                            <span className={styles.Icon}>
                                <PlusIcon height="16" width="16" />
                            </span>
                        </button>
                        <div className={styles.Answer}>
                            <div className={styles.AccordionContentWrapper}>{answer}</div>
                        </div>
                    </li>
                );
            })}
        </ol>
    );
};
