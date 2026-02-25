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
import { Tag } from "antd";

import { TooltipWithCopy } from "components";
import { defineBeanScopeColor } from "helpers";
import type { IBean } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single bean
     */
    bean: IBean;
}

export const BeanAccordionLabels = ({ bean }: IProps) => {
    const { beanName, className, scope, aliases } = bean;

    return (
        <>
            <div
                // These data attributes are later needed for scrolling. See scrollToAccordionById() function
                data-bean-entry
                data-bean-name={beanName}
                data-bean-class={className}
                data-bean-aliases={aliases}
                className={styles.AccordionHeader}
            >
                <div className={styles.AccordionHeaderContent}>
                    <div className={styles.BeanNameWrapper}>
                        <TooltipWithCopy text={beanName} />
                    </div>
                    <div className={styles.ClassName}>
                        <TooltipWithCopy text={className} />
                    </div>
                </div>
                <Tag variant="outlined" color={defineBeanScopeColor(scope)} className={styles.Scope}>
                    {scope}
                </Tag>
            </div>
        </>
    );
};
