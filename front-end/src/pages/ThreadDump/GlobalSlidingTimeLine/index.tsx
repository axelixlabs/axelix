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
import { useEffect, useState } from "react";

import { generateTimeSlots } from "helpers";
import { THREAD_DUMP_SLIDING_WINDOW_MS } from "utils";

import styles from "./styles.module.css";

export const GlobalSlidingTimeLine = () => {
    const [timeSlots, setTimeSlots] = useState<Date[]>([]);

    useEffect(() => {
        setTimeSlots(generateTimeSlots());

        const intervalId = setInterval(() => {
            setTimeSlots(generateTimeSlots());
        }, THREAD_DUMP_SLIDING_WINDOW_MS);

        return () => clearInterval(intervalId);
    }, []);

    return (
        <div className={styles.MainWrapper}>
            {timeSlots.map((timeSlot, index) => (
                <div className={styles.TimeSlot} key={index}>
                    {/* TODO: Consider the option of correct time display */}
                    {timeSlot.toLocaleTimeString([], { hour12: false })}
                </div>
            ))}
        </div>
    );
};
