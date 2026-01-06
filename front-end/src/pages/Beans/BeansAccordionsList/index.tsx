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
import { BeanAccordionLabels } from "pages/Beans/BeanAccordionLabels";
import { useLocation } from "react-router-dom";

import { Accordion } from "components";
import { normalizeHtmlElementId } from "helpers";
import type { IBean } from "models";

import { BeanAccordionChildren } from "../BeanAccordionChildren";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The list of beans
     */
    effectiveBeans: IBean[];
}

export const BeansAccordionsList = ({ effectiveBeans }: IProps) => {
    const { hash } = useLocation();

    return (
        <div className="AccordionsWrapper">
            {effectiveBeans.map((bean) => {
                const activeId = hash ? hash.replace("#", "") : null;
                const id = normalizeHtmlElementId(bean.beanName);
                const accordionExpanded = id === activeId;

                return (
                    <div id={id} key={id} className={styles.MainWrapper}>
                        <Accordion header={<BeanAccordionLabels bean={bean} />} accordionExpanded={accordionExpanded}>
                            <BeanAccordionChildren bean={bean} />
                        </Accordion>
                    </div>
                );
            })}
        </div>
    );
};
