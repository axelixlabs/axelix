"use client"
import styles from "./styles.module.css"
import { WhatCanDoCard } from "./WhatCanDoCard";

import { motion } from "motion/react"
import { whatCanDoCardsData, whatCanDoContainerVariants } from "@/utils";

export const WhatCanDo = () => {
    return (
        <section className={`MainContainer ${styles.MainWrapper}`}>
            <motion.h2
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 1, ease: "easeOut" }}
                className={`TextMedium ${styles.Title}`}
            >
                What Axelix can do
            </motion.h2>

            <motion.div
                className={styles.CardsWrapper}
                variants={whatCanDoContainerVariants}
                initial="hidden"
                whileInView="visible"
                viewport={{ once: true, amount: 0.2 }}
            >
                {whatCanDoCardsData.map((cardData) => (
                    <WhatCanDoCard cardData={cardData} key={cardData.title} />
                ))}
            </motion.div>
        </section>
    );
}