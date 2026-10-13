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

import { HowExactlyTopSection } from "./HowExactlyTopSection";
import { LayerCard } from "./LayerCard";
import { LAYERS } from "./layers";
import styles from "./styles.module.css";

const ZigzagRow = ({ layer, reversed }: { layer: (typeof LAYERS)[number]; reversed: boolean }) => {
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
                    {LAYERS.map((layer, layerIndex) => (
                        <ZigzagRow key={layer.index} layer={layer} reversed={layerIndex % 2 === 1} />
                    ))}
                </div>
            </div>
        </section>
    );
};
