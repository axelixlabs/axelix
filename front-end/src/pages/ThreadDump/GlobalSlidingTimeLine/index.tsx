/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
                    {timeSlot.toLocaleTimeString([], { hour12: false })}
                </div>
            ))}
        </div>
    );
};
