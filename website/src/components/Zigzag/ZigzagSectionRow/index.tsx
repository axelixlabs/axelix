"use client"
import Image from "next/image";
import Link from "next/link";
import { motion } from "motion/react"

import { IZigzagSectionData } from "@/models";
import styles from "./styles.module.css"

export interface IProps {
    section: IZigzagSectionData;
    index: number
}

export const ZigzagSectionRow = ({ section, index }: IProps) => {
    const { title, description, href, image } = section;
    const isEvenNumber = index % 2 === 0

    return (
        <motion.article
            initial={{ opacity: 0, x: isEvenNumber ? 50 : -50 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 1, ease: "easeOut" }}
            className={styles.MainWrapper}
        >
            <div className={styles.ContentWrapper}>
                <h3 className={`TextMedium ${styles.Title}`}>{title}</h3>
                <p className={styles.Description}>{description}</p>
                <Link href={href} className={styles.Link}>
                    See Docs
                    {/*TODO: Replace via svgr in future */}
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="16"
                        height="16"
                        viewBox="0 0 24 24"
                        strokeWidth="2"
                        stroke="currentColor"
                        fill="none"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        className={styles.ArrowIcon}
                    >
                        <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                        <polyline points="6 9 12 15 18 9" />
                    </svg>
                </Link>
            </div>

            <div className={styles.SectionImageWrapper}>
                <Image height="513" width="605" src={image} alt="Mock image" className={styles.SectionImage} />
            </div>
        </motion.article>
    )
}