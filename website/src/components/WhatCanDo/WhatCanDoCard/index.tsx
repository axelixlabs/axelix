import styles from "./styles.module.css"
import { motion } from "motion/react"
import { IWhatCanDoCardData } from "@/models";
import { whatCanDoCardVariants } from "@/utils";
import { Badge, Image } from "antd";
import { EyeOutlined } from '@ant-design/icons';
import Link from "next/link";

interface IProps {
    cardData: IWhatCanDoCardData
}

export const WhatCanDoCard = ({ cardData }: IProps) => {
    return (
        <motion.article className={styles.CardWrapper} variants={whatCanDoCardVariants}>
            <Badge.Ribbon text={cardData.category} color="#00ab55">
                <Link href={cardData.documentationLink} className={styles.ReadDocumentation}>
                    <div className={styles.Card}>
                        <header className={styles.HeaderWrapper}>
                            <h3 className={`TextSmall ${styles.CardTitle}`} dangerouslySetInnerHTML={{ __html: cardData.title }}/>
                            <p className={styles.CardDescription} dangerouslySetInnerHTML={{ __html: cardData.description }}/>
                        </header>
                        <div>
                            <div className={styles.ImageWrapper}>
                                <Image
                                    src={cardData.image}
                                    alt={cardData.title}
                                    preview={{
                                        cover: <EyeOutlined />,
                                    }}
                                    className={styles.Image}
                                    onClick={(e) => {
                                        e.stopPropagation()
                                        e.preventDefault()
                                    }}
                                />
                            </div>

                            <footer className={styles.ReadDocumentationWrapper}>
                                {/* TODO: Add target="_blank" and rel="noopener noreferrer" in the future if needed */}
                                <div className={styles.ReadDocumentation}>
                                    Docs
                                </div>
                            </footer>
                        </div>
                    </div>
                </Link>
            </Badge.Ribbon>
        </motion.article >
    )
}