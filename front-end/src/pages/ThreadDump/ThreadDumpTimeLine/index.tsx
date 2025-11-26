import { useEffect, useState } from "react";

import { generateTimeSlots } from "helpers";
import { TEN_MINUTES_MILLISECDONDS } from "utils";

import styles from "./styles.module.css";

export const ThreadDumpTimeLine = () => {
    const [timeSlots, setTimeSlots] = useState<Date[]>([]);

    useEffect(() => {
        generateTimeSlots(setTimeSlots);

        const intervalId = setInterval(() => {
            generateTimeSlots(setTimeSlots);
        }, TEN_MINUTES_MILLISECDONDS);

        return () => clearInterval(intervalId);
    }, []);

    return (
        <div className={styles.MainWrapper}>
            {timeSlots.map((timeSlot, index) => (
                <span className={styles.TimeSlot} key={index}>
                    {timeSlot.toLocaleTimeString([], { hour12: false })}
                </span>
            ))}
        </div>
    );
};
