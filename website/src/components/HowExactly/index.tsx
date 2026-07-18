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
import { useScrollReveal } from "@/hooks/useScrollReveal";

import { ApplicationYmlSnippet } from "./Snippets/ApplicationYmlSnippet";
import { DockerfileSnippet } from "./Snippets/DockerfileSnippet";
import { OrderServiceSnippet } from "./Snippets/OrderServiceSnippet";

import { HowExactlyTopSection } from "./HowExactlyTopSection";
import { LayerCard } from "./LayerCard";
import styles from "./styles.module.css";

const layers = [
    {
        index: "01",
        level: "JVM Deployment level",
        title: "Runtime Pitfalls",
        subtitle: "Inefficiencies that cost a lot at scale",
        items: [
            {
                id: "aot-cache",
                content: "Project Leyden adoption is missing",
                description:
                    "The Dockerfile on the right does not use Project Leyden. Project Leyden alone can contribute 15-30% of RSS footprint drop, and reduce startup time by 40-50%. Axelix detects that.",
            },
            {
                id: "compact-object-headers",
                content: "Compact Object Headers are not in use",
                description:
                    "Axelix will detect, that the app on the right is using Java 25. In Java 25, the Compact Object Headers feature is a stable production ready way to reduce the heap size by up to 20%. However, it is not enabled by default in Java 25 - Axelix will tell you about that.",
            },
            {
                id: "memory-pre-touching",
                content: "Not a Spring Boot efficient layered Docker image",
                description:
                    "The way most Docker images are built (the one on the right is not an exception) is by copying the fat jar into the image and use Spring Boot's URL classloader. That significantly increases the image size, along with the startup time.",
            },
            {
                id: "heap-gc-ergonomics",
                content: "GC ergonomics is not configured",
                description:
                    "Application on the right does not arrange any GC logging setup. Configuring GC logging is vrey important for overall performance analysis. Axelix will detect that.",
            },
        ],
        snippet: <DockerfileSnippet />,
    },
    {
        index: "02",
        level: "Code Level",
        title: "Enterprise Anti-Patterns",
        subtitle: "Access patterns that break under load",
        items: [
            {
                id: "n-plus-one",
                content: "Undetected N + 1",
                description:
                    'The iteration over retrieved Orders and subsequent "getLineItems()" causes the N + 1 problem. Axelix can detect that and make sure it is known by the team.',
            },
            {
                id: "blocking-call-inside-transaction",
                content: "Blocking call inside active transaction",
                description:
                    'The Spring\'s "RestClient" will perform an HTTP network call, which is going to cause the currently active thread to ' +
                    "go to interruptible sleep. The thread is also holding the connection open, which will lead to connection pool exhaustion. " +
                    "More importantly, such pattern invites hard-to-detect bugs and can cause inconsistency between systems because of the " +
                    "distributed data change. Axelix can detect those and will help you make sure such problems are gone.",
            },
            {
                id: "in-memory-pagination",
                content: "In-memory pagination performed by Hibernate",
                description:
                    "Up until Hibernate 7.4 (and most deployments od not use Hibernate 7) queries with Spring Data's Pageable and JOIN FETCH " +
                    "caused the Hibernate to fetch all the records into memory, and then paginate in-memory. That is generally extremely bad, " +
                    "since it explodes the Java heap and introduces sever latency. To be clear - Hibernate itself issues a warning about that, " +
                    "but it cannot fix it for you. Axelix will detect that.",
            },
            {
                id: "fetch-strategies",
                content: "Fetch strategies made explicit",
                description: (
                    <>
                        By default, <code>@ManyToOne</code> and <code>@OneToOne</code> relations are fetched eagerly.
                        There were hopes that JPA 4 will change the fetch types, but it did not (for backward
                        compatibility purposes). Still, a lot of applications suffer from EAGER fetching, and it was, as
                        noted by the Hibernate team themselves, was a historical mistake. Axelix will help you to keep
                        that technical debt under control.
                    </>
                ),
            },
        ],
        snippet: <OrderServiceSnippet />,
    },
    {
        index: "03",
        level: "Application Configuration Level",
        title: "Configuration Problems",
        subtitle: "Defaults that quietly cost you, surfaced.",
        items: [
            {
                id: "OISV",
                content: "Enabled OSIV (Open Session in View)",
                description:
                    "This is probably one of the most dangerous default that Spring Boot applications have. It is enabled by default " +
                    "just for quickstart, but a lot of deployments still have it in production. OSIV leads to connections leaks, unexpected " +
                    "lazy loading and so on. For production systems, it absolutely must be turned off. Axelix will help you ensure that.",
            },
            {
                id: "virtual-threads",
                content: "Virtual Threads are not adopted",
                description:
                    "By default, for backward compatibility purposes, Virtual Threads are not enabled in Spring Boot apps. Still, " +
                    "at least since Java 24 they absolutely must be adopted by the Spring Boot applications. They generally significantly " +
                    "improve throughput without requiring the application re-write. Axelix will help you to keep track that the entire " +
                    "ecosystem adopted Virtual Threads.",
            },
            {
                id: "spring-jpa-show-sql",
                content: "Spring JPA show-sql enabled",
                description: (
                    <>
                        There are a lot of bad properties that people still use in production for various purposes, and{" "}
                        <code>spring.jpq.show-sql=true</code> is one of them. It is bad, since it arranges the
                        synchronous output to stdout, which bypasses the logging system and degrades performance. There
                        are other, more reliable and safe ways to log SQL queries.
                    </>
                ),
            },
            {
                id: "spring-actuator-endpoints-exposed-via-star",
                content: "Actuator endpoints exposed via wildcard",
                description: (
                    <>
                        This is one of the most common security breaches into Spring Boot applications. Although it is
                        very compelling to use &#34;*&#34; as the value for the exposed endpoints, it also brings as
                        security risk, such as that any library that introduces previously unknown Actuator endpoint to
                        the classpath will automatically be exposed, which can easily lead to sensitive data leakage, or
                        even RCE vulnerability.
                    </>
                ),
            },
        ],
        snippet: <ApplicationYmlSnippet />,
    },
];

const ZigzagRow = ({ layer, reversed }: { layer: (typeof layers)[number]; reversed: boolean }) => {
    const [ref, visible] = useScrollReveal<HTMLDivElement>();
    const { index, level, title, subtitle, items, snippet } = layer;

    return (
        <div
            ref={ref}
            className={`${styles.Row} ${reversed ? styles.Reversed : ""} ${visible ? styles.RowVisible : ""}`}
        >
            <LayerCard index={index} level={level} title={title} subtitle={subtitle} items={items} />
            {snippet}
        </div>
    );
};

export const HowExactly = () => {
    return (
        <section className={styles.MainWrapper} id="how">
            <div className="MainContainer">
                <HowExactlyTopSection />

                <div className={styles.Zigzag}>
                    {layers.map((layer, layerIndex) => (
                        <ZigzagRow key={layer.index} layer={layer} reversed={layerIndex % 2 === 1} />
                    ))}
                </div>
            </div>
        </section>
    );
};
