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
import { useVirtualizer } from "@tanstack/react-virtual";

import { type Dispatch, type SetStateAction, useEffect, useRef } from "react";
import { useLocation } from "react-router";

import { Accordion } from "components";
import { findBeanBySearchSubject } from "helpers";
import { ESearchSubject, type IBean } from "models";

import { BeanAccordionChildren } from "../BeanAccordionChildren";
import { BeanAccordionLabels } from "../BeanAccordionLabels";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The list of beans to display
     */
    effectiveBeans: IBean[];

    /**
     * Full list of beans used for search
     */
    beansFeed: IBean[];

    /**
     * Selected bean
     */
    selectedBean: IBean | null;

    /**
     * Setter to set the selected bean
     */
    setSelectedBean: Dispatch<SetStateAction<IBean | null>>;
}

export const BeansAccordionsList = ({ effectiveBeans, selectedBean, setSelectedBean, beansFeed }: IProps) => {
    const { hash } = useLocation();

    const ref = useRef<HTMLDivElement>(null);

    const rowVirtualizer = useVirtualizer({
        count: effectiveBeans.length,
        getScrollElement: () => ref.current,
        estimateSize: () => 77,
        scrollPaddingStart: 80,
    });

    useEffect(() => {
        if (hash) {
            const foundBean = findBeanBySearchSubject(hash, ESearchSubject.BEAN_NAME_BY_HASH, beansFeed);
            setSelectedBean(foundBean);
        }
    }, []);

    const virtualItems = rowVirtualizer.getVirtualItems();

    return (
        <>
            <div ref={ref} className={`AccordionsWrapper ${styles.MainWrapper}`}>
                <div
                    style={{
                        height: `${rowVirtualizer.getTotalSize()}px`,
                    }}
                    className={styles.InnerWrapper}
                >
                    {virtualItems.map(({ key, index, start }) => {
                        const bean = effectiveBeans[index];

                        return (
                            <div
                                key={key}
                                data-index={index}
                                ref={rowVirtualizer.measureElement}
                                className={styles.VirtualItem}
                                style={{
                                    transform: `translateY(${start}px)`,
                                }}
                            >
                                <Accordion
                                    header={<BeanAccordionLabels bean={bean} />}
                                    accordionExpanded={Boolean(selectedBean)}
                                >
                                    <BeanAccordionChildren
                                        bean={bean}
                                        beansFeed={beansFeed}
                                        setSelectedBean={setSelectedBean}
                                    />
                                </Accordion>
                            </div>
                        );
                    })}
                </div>
            </div>
        </>
    );
};
