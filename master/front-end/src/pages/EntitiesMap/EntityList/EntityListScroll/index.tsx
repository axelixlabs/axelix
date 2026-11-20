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
import { type CSSProperties, type PropsWithChildren, useEffect, useRef, useState } from "react";

import panelStyles from "../../styles.module.css";

import styles from "./styles.module.css";

export const EntityListScroll = ({ children }: PropsWithChildren) => {
    const viewport = useRef<HTMLDivElement>(null);
    const content = useRef<HTMLDivElement>(null);
    const [metrics, setMetrics] = useState({ maxScroll: 0, thumbHeight: 0 });
    const [scrollTop, setScrollTop] = useState(0);

    useEffect(() => {
        const scroller = viewport.current;
        const list = content.current;

        if (!scroller || !list) {
            return;
        }

        const observer = new ResizeObserver(() => {
            const { clientHeight, scrollHeight, scrollTop: currentTop } = scroller;
            setMetrics({
                maxScroll: scrollHeight - clientHeight,
                thumbHeight: (clientHeight / Math.max(scrollHeight, 1)) * clientHeight,
            });
            setScrollTop(currentTop);
        });

        observer.observe(scroller);
        observer.observe(list);
        return () => observer.disconnect();
    }, []);

    return (
        <section className={`${panelStyles.Panel} ${styles.EntityList}`} aria-labelledby="entities-map-list">
            <div
                id="entities-map-scroll"
                ref={viewport}
                className={styles.EntityListViewport}
                onScroll={(event) => setScrollTop(event.currentTarget.scrollTop)}
            >
                <div ref={content}>{children}</div>
            </div>
            {metrics.maxScroll > 0 && (
                <input
                    type="range"
                    aria-labelledby="entities-map-list"
                    aria-controls="entities-map-scroll"
                    aria-orientation="vertical"
                    className={styles.EntityListScrollbar}
                    style={{ "--thumb-height": `${metrics.thumbHeight}px` } as CSSProperties}
                    min={0}
                    max={metrics.maxScroll}
                    step="any"
                    value={scrollTop}
                    onChange={(event) => {
                        const top = event.currentTarget.valueAsNumber;
                        viewport.current?.scrollTo({ top, behavior: "instant" });
                        setScrollTop(top);
                    }}
                />
            )}
        </section>
    );
};
