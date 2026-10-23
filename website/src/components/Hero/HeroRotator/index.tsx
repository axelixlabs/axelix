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
import { useEffect, useState } from "react";

import styles from "./styles.module.css";

// Stub phrases, to be replaced with the final copy.
const PHRASES = [
    { lead: "See", text: "which property value won, and where it came from" },
    { lead: "Catch", text: "N+1 queries and eager fetching" },
    { lead: "Stop", text: "them before production, with Enterprise" },
];

const ROTATE_INTERVAL_MS = 2500;

const nextIndex = (current: number): number => (current + 1) % PHRASES.length;

export const HeroRotator = () => {
    const [index, setIndex] = useState<number>(0);

    useEffect(() => {
        const interval = setInterval(() => setIndex(nextIndex), ROTATE_INTERVAL_MS);

        return () => clearInterval(interval);
    }, []);

    const { lead, text } = PHRASES[index];

    return (
        <p className={styles.Rotator} aria-live="polite">
            <span>{lead}</span>
            <span key={index} className={styles.Phrase}>
                {text}
            </span>
        </p>
    );
};
