"use client"
import Link from "next/link"
import { InstallationContent } from "./InstallationContent"
import styles from "./styles.module.css"
import { useState } from "react"
import { IInstallationInstructions } from "@/models"
import { motion } from "motion/react"
import { installationOptions } from "@/utils"

export const Installation = () => {
    const [selectedOption, setSelectedOption] = useState<IInstallationInstructions>(installationOptions[0])

    return (
        <motion.section
            id="installation"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 1, ease: "easeOut" }}
            className={`MainContainer ${styles.MainWrapper}`}
        >
            <h2 className="TextMedium">Installation</h2>
            <div className={styles.GuideWrapper}>
                <div className={styles.GuidePanel}>
                    <ul className={styles.TabsWrapper}>
                        {installationOptions.map((option) => (
                            <li
                                key={option.option}
                            >
                                <button
                                    type="button"
                                    onClick={() => setSelectedOption(option)}
                                    className={`${styles.Tab} ${selectedOption === option ? styles.ActiveTab : ""}`}
                                >
                                    {option.option}
                                </button>
                            </li>
                        ))}
                    </ul>

                    {/* FIX: Fix in future */}
                    <div>
                        <p className={styles.InstallDescription}>
                            {selectedOption.description}
                        </p>

                        {/* TODO: Add target="_blank" and rel="noopener noreferrer" in the future if needed */}
                        {/* TODO: Replace Link via a if that needed */}
                        <Link href="https://spring.io/" className={styles.ReadDocumentation}>
                            Read Documentation
                        </Link>
                    </div>
                </div>

                <InstallationContent instructions={selectedOption} key={selectedOption.option} />
            </div>
        </motion.section>
    )
}