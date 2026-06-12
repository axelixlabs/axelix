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
import { IBlogCardItem } from "@/models";

import Link from "next/link";

import { Authors } from "../Authors";
import { DateMeta } from "../DateMeta";
import { TagRow } from "../TagRow";

import styles from "./styles.module.css";

interface IProps {
    item: IBlogCardItem;
}

export const PostRow = ({ item }: IProps) => {
    return (
        <Link className={styles.PostRow} href={item.href}>
            <div className={styles.Rbody}>
                <TagRow tags={item.tags} />
                <DateMeta date={item.date} readingMinutes={item.readingMinutes} />
                <h3>{item.title}</h3>
                {item.description && <p className={styles.Exc}>{item.description}</p>}
                <Authors authors={item.authors} />
            </div>
            {item.coverSrc ? (
                <div className={styles.Rcover}>
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img src={item.coverSrc} alt="" />
                </div>
            ) : (
                <div className={`${styles.Rcover} ${styles.CoverPh}`} />
            )}
        </Link>
    );
};
