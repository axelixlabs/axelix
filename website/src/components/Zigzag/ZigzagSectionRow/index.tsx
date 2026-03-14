"use client"
import { motion } from "motion/react"

import { IZigzagSectionData } from "@/models";
import styles from "./styles.module.css"
import {Image} from "antd";
import {EyeOutlined} from "@ant-design/icons";
import { ArrowIcon } from "@/assets";

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
                <a href={href} target="_blank" rel="noreferrer noopener" className={styles.Link}>
                    See Docs
                    <ArrowIcon className={styles.ArrowIcon}/>
                </a>
            </div>

            <div className={styles.SectionImageWrapper}>
                <Image height="513" width="605" src={image.src} preview={{
                    cover: <EyeOutlined/>
                }} alt="Mock image" className={styles.SectionImage} />
            </div>
        </motion.article>
    )
}