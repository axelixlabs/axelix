"use client"
import { useState } from "react";
import styles from "./styles.module.css";
import { IFAQItem } from "@/models";

interface IProps {
    faqItems: IFAQItem[]
}

export const FAQAccordions = ({ faqItems }: IProps) => {
    const [openedAccordionIndex, setOpenedAccordionIndex] = useState<number | null>(null);

    const openAccordion = (index: number): void => {
        setOpenedAccordionIndex(openedAccordionIndex === index ? null : index);
    };

    return (
        <section className={`MainContainer ${styles.MainWrapper}`}>
            <div className={styles.InnerWrapper}>
                <h2 id="faq-title" className={`TextMedium ${styles.Title}`}>Frequently asked questions</h2>
                <dl>
                    {faqItems.map(({ question, answer }, index) => {
                        const isOpen = openedAccordionIndex === index;
                        return (
                            <div
                                key={question}
                                className={`${styles.SingleAccordionWrapper} ${isOpen ? styles.ActiveAccordionWrapper : ""}`}
                            >
                                <dt>
                                    <button
                                        type="button"
                                        onClick={() => openAccordion(index)}
                                        className={`${styles.InteractiveHeader} ${isOpen ? styles.ActiveHeader : ""}`}
                                    >
                                        {question}
                                        <span className={`${styles.Icon} ${isOpen ? styles.ActiveIcon : ""}`}>
                                            {isOpen ? "-" : "+"}
                                        </span>
                                    </button>
                                </dt >
                                <dd className={`${styles.ContentWrapper} ${isOpen ? styles.ActiveContentWrapper : ""}`}>
                                    <div className={styles.Content}>
                                        {answer}
                                    </div>
                                </dd>
                            </div>
                        );
                    })}
                </dl >
            </div>
        </section>
    );
}