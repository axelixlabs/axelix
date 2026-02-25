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
import { BeanAccordionLabels } from "pages/Beans/BeanAccordionLabels";
import { useLocation } from "react-router-dom";

import { Accordion } from "components";
import { normalizeHtmlElementId } from "helpers";
import type { IBean } from "models";

import { BeanAccordionChildren } from "../BeanAccordionChildren";

interface IProps {
    /**
     * The list of beans
     */
    effectiveBeans: IBean[];
}

export const BeansAccordionsList = ({ effectiveBeans }: IProps) => {
    const { hash } = useLocation();

    return (
        <>
            <div className="AccordionsWrapper">
                {effectiveBeans.map((bean) => {
                    const activeId = hash ? hash.replace("#", "") : null;
                    const id = normalizeHtmlElementId(bean.beanName);
                    const accordionExpanded = id === activeId;

                    return (
                        <div id={id} key={id}>
                            <Accordion
                                header={<BeanAccordionLabels bean={bean} />}
                                accordionExpanded={accordionExpanded}
                            >
                                <BeanAccordionChildren bean={bean} />
                            </Accordion>
                        </div>
                    );
                })}
            </div>
        </>
    );
};
