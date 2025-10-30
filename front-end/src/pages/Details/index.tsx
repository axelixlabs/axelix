import { useMemo } from "react";

import { detailsCardsPreferredOrder } from "utils";

import { DetailsCard } from "./DetailsCard";
import styles from "./styles.module.css";

const data = {
    git: {
        commitShaShort: "string",
        branch: "string",
        authorName: "string",
        authorEmail: "string",
        commitTimestamp: "string",
    },
    runtime: {
        javaVersion: "string",
        jdkVendor: "string",
        garbageCollector: "string",
        kotlinVersion: "string",
    },
    spring: {
        springBootVersion: "string",
        SpringFrameworkVersion: "string",
        SpringCloudVersion: "string",
    },
    build: {
        artifact: "string",
        version: "string",
        group: "string",
        time: "string",
    },
    os: {
        name: "Windows",
        version: "string",
        arch: "string",
    },
};

const Details = () => {
    const cardsData = useMemo(() => {
        return Object.entries(data)
            .map(([title, content]) => ({
                title: title,
                content: Object.entries(content || {}),
            }))
            .sort((currentCardData, nextCardData) => {
                const currentIndex = detailsCardsPreferredOrder.indexOf(currentCardData.title);
                const nextIndex = detailsCardsPreferredOrder.indexOf(nextCardData.title);
                return currentIndex - nextIndex;
            });
    }, []);

    return (
        <>
            <div className={styles.MainTitle}>interaction-management-system-mq-impl-5dc49b9459-5vcgz</div>

            <div className={styles.InnerWrapper}>
                <div className={styles.ColumnWrapper}>
                    {cardsData.slice(0, 2).map(({ title, content }) => (
                        <DetailsCard title={title} content={content} key={title} />
                    ))}
                </div>

                <div className={styles.ColumnWrapper}>
                    {cardsData.slice(2, 5).map(({ title, content }) => (
                        <DetailsCard title={title} content={content} key={title} />
                    ))}
                </div>
            </div>
        </>
    );
};

export default Details;
