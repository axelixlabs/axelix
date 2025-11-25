import { useEffect, useState } from "react";

import styles from "./styles.module.css";

export const ThreadDumpTimeLine = () => {
    const [timeSlots, setTimeSlots] = useState<Date[]>([]);

    useEffect(() => {
        const now = new Date();

        // 15 seconds interval between time slots
        const stepMilliseconds = 15 * 1000;

        // 10 minutes ahead from now
        const endTime = new Date(now.getTime() + 10 * 60 * 1000);
        const slots: Date[] = [];

        let currentTime = new Date(now);

        // generate all time slots from current time to endTime with a 15-second interval
        while (currentTime <= endTime) {
            slots.push(new Date(currentTime));
            currentTime = new Date(currentTime.getTime() + stepMilliseconds);
        }

        setTimeSlots(slots);
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
