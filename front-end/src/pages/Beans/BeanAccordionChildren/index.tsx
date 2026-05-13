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
import type { Dispatch, SetStateAction } from "react";

import { type IBean } from "models";

import { BeanBooleanFlag } from "./BeanBooleanFlag";
import { BeanDependencies } from "./BeanDependencies";
import { BeanProxyType } from "./BeanProxyType";
import { BeanSimpleList } from "./BeanSimpleList";
import { BeanSource } from "./BeanSource";
import styles from "./styles.module.css";

interface IProps {
    /**
     * Single bean
     */
    bean: IBean;

    /**
     * Full list of beans used for search
     */
    beansFeed: IBean[];

    /**
     * Setter to set the selected bean
     */
    setSelectedBean: Dispatch<SetStateAction<IBean | null>>;
}

export const BeanAccordionChildren = ({ bean, beansFeed, setSelectedBean }: IProps) => {
    const { dependencies, aliases, qualifiers, isLazyInit, isPrimary } = bean;

    return (
        <>
            <div className={styles.AccordionBody}>
                {dependencies.length > 0 && (
                    <BeanDependencies
                        dependencies={dependencies}
                        beansFeed={beansFeed}
                        setSelectedBean={setSelectedBean}
                    />
                )}
                {aliases.length > 0 && <BeanSimpleList valuesTag="aliases" values={aliases} />}
                {qualifiers.length > 0 && <BeanSimpleList valuesTag="qualifiers" values={qualifiers} />}
                <BeanProxyType proxyType={bean.proxyType} />
                <BeanBooleanFlag valueTag={"isLazyInitBean"} value={isLazyInit} />
                <BeanBooleanFlag valueTag={"isPrimaryBean"} value={isPrimary} />
                <BeanSource bean={bean} beansFeed={beansFeed} setSelectedBean={setSelectedBean} />
            </div>
        </>
    );
};
