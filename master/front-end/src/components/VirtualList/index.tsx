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
import { Listy } from "antd";
import { type ReactNode, useEffect, useRef, useState } from "react";

import styles from "./styles.module.css";

interface IProps<T> {
    /**
     * Data source of the list
     */
    items: T[];

    /**
     * Renders a single row
     */
    itemRender: (item: T, index: number) => ReactNode;

    /**
     * Unique key of an item
     */
    rowKey: keyof T;
}

export const VirtualList = <T,>({ items, itemRender, rowKey }: IProps<T>) => {
    const listWrapperRef = useRef<HTMLDivElement>(null);
    const [listHeight, setListHeight] = useState<number>(0);

    useEffect(() => {
        const listContainer = listWrapperRef.current;
        if (!listContainer) {
            return;
        }

        const observer = new ResizeObserver((entries) => {
            const entry = entries[0];
            if (entry) {
                setListHeight(entry.contentRect.height);
            }
        });

        observer.observe(listContainer);
        return () => observer.disconnect();
    }, []);

    return (
        <>
            <div ref={listWrapperRef} className={styles.MainWrapper}>
                {listHeight > 0 && (
                    <Listy<T>
                        items={items}
                        height={listHeight}
                        virtual
                        rowKey={rowKey}
                        itemRender={itemRender}
                        styles={{
                            item: {
                                padding: 0,
                            },
                        }}
                    />
                )}
            </div>
        </>
    );
};
