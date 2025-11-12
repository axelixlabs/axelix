import { EBeanOrigin } from "models";

import styles from "../components/Accordion/styles.module.css";

export function scrollToAccordionById(targetBeanId: string | undefined, source?: string): void {
    if (!targetBeanId) {
        return;
    }

    const elementsWithBeanIds = document.querySelectorAll<HTMLElement>("[data-bean-ids]");

    for (const element of elementsWithBeanIds) {
        const ids = element.dataset.beanIds?.split(" ") || [];

        const isMatchingBean = ids.some(
            (id) => targetBeanId === id || (source === EBeanOrigin.FACTORY_BEAN && id === `alias-${targetBeanId}`),
        );

        if (isMatchingBean) {
            const accordion = element.closest<HTMLElement>(`.${styles.MainWrapper}`) ?? element;
            const isOpen = accordion.classList.contains(styles.Open);

            const accordionHeader = accordion.querySelector<HTMLElement>(`.${styles.HeaderWrapper}`);
            if (accordionHeader && !isOpen) {
                accordionHeader.click();
            }

            element.scrollIntoView({ behavior: "smooth", block: "start" });

            return;
        }
    }
}
