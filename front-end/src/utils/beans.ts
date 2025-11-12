import { ESearchSubject } from "models";

import styles from "../components/Accordion/styles.module.css";

export function scrollToAccordionById(query: string, searchSubject: ESearchSubject): void {
    if (!query) {
        return;
    }

    const elementsWithBeanIds = document.querySelectorAll<HTMLElement>("[data-bean-entry]");

    for (const element of elementsWithBeanIds) {
        const dataset = element.dataset;
        const beanName = dataset.beanName;
        const beanClass = dataset.beanClass;
        const beanAliases = dataset.beanAliases?.split(",") || [];

        const isMatchingBean =
            (searchSubject === ESearchSubject.BEAN_CLASS && beanClass === query) ||
            (searchSubject === ESearchSubject.BEAN_NAME_OR_ALIAS &&
                (beanName === query || beanAliases.some((alias) => alias === query)));

        if (isMatchingBean) {
            const accordion = element.closest<HTMLElement>(`.${styles.MainWrapper}`) ?? element;
            const isOpen = accordion.classList.contains(styles.Open);

            const accordionHeader = accordion.querySelector<HTMLElement>(`.${styles.HeaderWrapper}`);
            if (accordionHeader && !isOpen) {
                accordionHeader.click();
            }

            element.scrollIntoView();

            return;
        }
    }
}
