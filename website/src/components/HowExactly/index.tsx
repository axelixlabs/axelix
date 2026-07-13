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
import { ApplicationYmlSnippet } from "./Snippets/ApplicationYmlSnippet";
import { DockerfileSnippet } from "./Snippets/DockerfileSnippet";
import { OrderServiceSnippet } from "./Snippets/OrderServiceSnippet";

import { HowExactlyTopSection } from "./HowExactlyTopSection";
import { LayerCard } from "./LayerCard";
import styles from "./styles.module.css";

const layers = [
    {
        index: "01",
        level: "JVM level",
        title: "Runtime signals",
        subtitle: "Where startup and memory are quietly bleeding.",
        items: [
            { id: "aot-cache", content: "AOT Cache / App CDS adoption" },
            { id: "compact-object-headers", content: "Compact Object Headers usage" },
            { id: "memory-pre-touching", content: "Eager memory pre-touching" },
            { id: "heap-gc-ergonomics", content: "Right-sized heap & GC ergonomics" },
        ],
        snippet: <DockerfileSnippet />,
    },
    {
        index: "02",
        level: "Persistence",
        title: "Query hotspots",
        subtitle: "Access patterns that break under load.",
        items: [
            { id: "n-plus-one", content: "N+1 selects mitigated" },
            { id: "in-memory-pagination", content: "In-memory pagination eliminated" },
            { id: "cartesian-products", content: "Cartesian products removed" },
            { id: "fetch-strategies", content: "Fetch strategies made explicit" },
        ],
        snippet: <OrderServiceSnippet />,
    },
    {
        index: "03",
        level: "Spring Boot",
        title: "Framework smells",
        subtitle: "Defaults that quietly cost you, surfaced.",
        items: [
            { id: "osiv", content: "OSIV (Open Session in View) disabled" },
            { id: "lazy-beans", content: "Lazy bean initialization where safe" },
            {
                id: "async-thread-pools",
                content: (
                    <>
                        <code>@Async</code> thread pools controlled
                    </>
                ),
            },
            { id: "jmx-autoconfig", content: "JMX & unused autoconfig trimmed" },
        ],
        snippet: <ApplicationYmlSnippet />,
    },
];

export const HowExactly = () => {
    return (
        <section className={styles.MainWrapper} id="how">
            <div className="MainContainer">
                <HowExactlyTopSection />

                <div className={styles.Zigzag}>
                    {layers.map(({ index, level, title, subtitle, items, snippet }, layerIndex) => (
                        <div key={index} className={`${styles.Row} ${layerIndex % 2 === 1 ? styles.Reversed : ""}`}>
                            <LayerCard index={index} level={level} title={title} subtitle={subtitle} items={items} />
                            {snippet}
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
};
