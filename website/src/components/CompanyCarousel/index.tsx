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
import Image from "next/image";
import { CSSProperties } from "react";

import styles from "./styles.module.css";

const COMPANIES = [
    { name: "Convex", logo: "convex.com.png", size: [1280, 1280], bounds: [22, 0, 1235, 1280] },
    { name: "Gratia", logo: "gogratia.com.png", size: [512, 512], bounds: [0, 0, 512, 512] },
    { name: "Neo4j", logo: "neo4j.com.png", size: [834, 313], bounds: [13, 23, 808, 267] },
    { name: "OFX", logo: "ofx.com.png", size: [1551, 629], bounds: [0, 0, 1551, 629] },
    { name: "Productboard", logo: "productboard.com.png", size: [560, 300], bounds: [0, 111, 560, 74] },
    { name: "Bloomberg", logo: "bloomberg.com.png", size: [3840, 2160], bounds: [16, 727, 3808, 705] },
    { name: "Miro", logo: "miro.com.png", size: [1024, 576], bounds: [37, 113, 950, 350] },
    { name: "Wiz", logo: "wiz.io.png", size: [1418, 681], bounds: [0, 0, 1418, 681] },
];

export const CompanyCarousel = () => {
    return (
        <section className={styles.Section} aria-labelledby="company-carousel-title">
            <div className={styles.Container}>
                <h2 id="company-carousel-title" className={styles.Title}>
                    Trusted by Java teams across the globe
                </h2>
                <div className={styles.Viewport}>
                    <div className={styles.Track}>
                        {[false, true].map((duplicate) => (
                            <ul key={String(duplicate)} className={styles.Logos} aria-hidden={duplicate || undefined}>
                                {COMPANIES.map(
                                    ({
                                        name,
                                        logo,
                                        size: [width, height],
                                        bounds: [left, top, visibleWidth, visibleHeight],
                                    }) => {
                                        // Lay out the visible artwork, excluding transparent padding in the supplied files.
                                        const logoWidth = Math.min(176, (48 * visibleWidth) / visibleHeight);
                                        const logoHeight = (logoWidth * visibleHeight) / visibleWidth;

                                        return (
                                            <li
                                                key={name}
                                                className={styles.Logo}
                                                style={
                                                    {
                                                        "--logo-width": `${logoWidth}px`,
                                                        "--logo-height": `${logoHeight}px`,
                                                    } as CSSProperties
                                                }
                                            >
                                                <Image
                                                    src={`/companies/${logo}`}
                                                    alt={duplicate ? "" : name}
                                                    width={width}
                                                    height={height}
                                                    className={styles.Image}
                                                    style={{
                                                        width: `${(width / visibleWidth) * 100}%`,
                                                        height: `${(height / visibleHeight) * 100}%`,
                                                        left: `${(-left / visibleWidth) * 100}%`,
                                                        top: `${(-top / visibleHeight) * 100}%`,
                                                    }}
                                                />
                                            </li>
                                        );
                                    },
                                )}
                            </ul>
                        ))}
                    </div>
                </div>
            </div>
        </section>
    );
};
