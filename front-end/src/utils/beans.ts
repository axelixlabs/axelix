/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import styles from "components/Accordion/styles.module.css";

import { ESearchSubject } from "models";

const openAndScrollToAccordion = (element: HTMLElement): void => {
    const accordion = element.closest<HTMLElement>(`.${styles.MainWrapper}`) ?? element;
    const isOpen = accordion.classList.contains(styles.Open);

    const accordionHeader = accordion.querySelector<HTMLElement>(`.${styles.HeaderWrapper}`);
    if (accordionHeader && !isOpen) {
        accordionHeader.click();
    }

    element.scrollIntoView();
};

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
            openAndScrollToAccordion(element);
        }
    }
}
