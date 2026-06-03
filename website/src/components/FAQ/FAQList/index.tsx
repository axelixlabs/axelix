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
                The open-source console for debugging, observing and operating Spring Boot microservices in production.{" "}
                <em>Every capability is exposed twice</em> — to engineers through a web console, and to AI agents
                through an embedded MCP server. A single role model gates both.
            </p>
        ),
    },
    {
        question: "Which Spring Boot versions are supported?",
        answer: (
            <p>
                Spring Boot 2, 3 and 4 — through dedicated starter artifacts (<code>axelix-spring-boot-2-starter</code>,{" "}
                <code>-3-starter</code>, <code>-4-starter</code>). The master runs on JVM 11–25.{" "}
                <em>No JVM agent, no flags, no custom boot order.</em>
            </p>
        ),
    },
    {
        question: "Is it really free?",
        answer: (
            <p>
                Yes. Axelix OSS is licensed under <em>LGPL-3.0</em> — link it into your production apps, fork it, ship
                your own changes. We&apos;re building <em>Axelix Enterprise</em> on top (extensions and paid support for
                teams that want them), but the open core stays open.
            </p>
        ),
    },
    {
        question: "Is it safe to run in production?",
        answer: (
            <p>
                It&apos;s designed for production. The master speaks to your services over an authenticated channel; a
                single role model gates both human engineers and AI agents — each identity sees only the data and
                actions its role permits. Destructive operations are explicit and confirmable.
            </p>
        ),
    },
    {
        question: "How is it different from Micrometer / Actuator?",
        answer: (
            <p>
                Actuator exposes the data; <em>Axelix exposes the verbs.</em> We build on the actuator endpoints your
                apps already have, then add what actuator doesn&apos;t: a unified fleet console, an MCP server, runtime
                mutation under a role model, transactional inspection with SQL timelines, and a clear story for AI
                agents.
            </p>
        ),
    },
    {
        question: "What is Axelix Enterprise?",
        answer: (
            <p>
                We&apos;re building something bigger. <em>Stay tuned.</em>
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
