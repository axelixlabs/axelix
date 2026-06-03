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
import styles from "./styles.module.css";

export const HeroHeadline = () => {
    return (
        <>
            <h1 className={styles.Title}>
                Go <span className={styles.GreenText}>AI-Native</span> with{" "}
                <span className={styles.GreenText}>Spring&nbsp;Boot</span>: The Secret to{" "}
                <span className={styles.UnderlinedText}>Skyrocketing</span> Engineering Velocity
            </h1>

            <p className={styles.Lede}>
                Axelix is the <span className={styles.GreenTextBold}>AI-Native</span>, OSS solution for{" "}
                <em className={styles.AccentedText}>
                    debugging, observing and operating mission-critical Spring Boot microservices.
                </em>{" "}
                Every capability is exposed twice — to human engineers through the web, and to AI agents through an
                embedded MCP server.
            </p>
        </>
    );
};
