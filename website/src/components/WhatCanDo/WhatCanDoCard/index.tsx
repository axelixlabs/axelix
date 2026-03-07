import styles from "./styles.module.css"
import { motion } from "motion/react"
import { IWhatCanDoCardData } from "@/models";
import { whatCanDoCardVariants } from "@/utils";
import { Badge, Image } from "antd";
import { EyeOutlined } from '@ant-design/icons';

interface IProps {
    cardData: IWhatCanDoCardData
}

export const WhatCanDoCard = ({ cardData }: IProps) => {
    return (
        <motion.article className={styles.CardWrapper} variants={whatCanDoCardVariants}>
            <Badge.Ribbon text={cardData.category} color="#00ab55">
                <a href={cardData.documentationLink} target="_blank" rel="noopener noreferrer">
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
                                        e.preventDefault()
                                    }}
                                />
                            </div>

                            <footer className={styles.ReadDocumentationWrapper}>
                                <div className={styles.DocsWrapper}>
                                    Docs
                                </div>
                            </footer>
                        </div>
                    </div>
                </a>
            </Badge.Ribbon>
        </motion.article >
    )
}