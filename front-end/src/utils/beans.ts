/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { ESearchSubject } from "models";

import styles from "../components/Accordion/styles.module.css";

function openAndScrollToAccordion(element: HTMLElement) {
    const accordion = element.closest<HTMLElement>(`.${styles.MainWrapper}`) ?? element;
    const isOpen = accordion.classList.contains(styles.Open);

    const accordionHeader = accordion.querySelector<HTMLElement>(`.${styles.HeaderWrapper}`);
    if (accordionHeader && !isOpen) {
        accordionHeader.click();
    }

    element.scrollIntoView();

    return;
}

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
