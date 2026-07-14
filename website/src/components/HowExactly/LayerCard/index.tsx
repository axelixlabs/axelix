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
import { ReactNode } from "react";

import { Problem } from "../Problem";

import styles from "./styles.module.css";

export interface IBullet {
    id: string;
    content: ReactNode;
    description: ReactNode;
}

interface IProps {
    index: string;
    level: string;
    title: string;
    subtitle: string;
    items: IBullet[];
}

export const LayerCard = ({ index, level, title, subtitle, items }: IProps) => {
    return (
        <article className={styles.MainWrapper}>
            <span className={styles.Level}>
                <span className={styles.Index}>{index}</span> {level}
            </span>
            <h3 className={styles.Title}>{title}</h3>
            <p className={styles.Subtitle}>{subtitle}</p>
            <ul className={styles.List}>
                {items.map(({ id, content, description }) => (
                    <Problem key={id} title={content} description={description} />
                ))}
            </ul>
        </article>
    );
};
