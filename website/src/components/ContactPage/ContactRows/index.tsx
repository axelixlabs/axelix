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
import { getLinkTargetProps } from "../../../utils";

import styles from "./styles.module.css";

export interface IContactRow {
    label: string;
    value: string;
    note: string;
    href: string;
    external?: boolean;
}

interface IProps {
    rows: IContactRow[];
}

export const ContactRows = ({ rows }: IProps) => {
    return (
        <div className={styles.MainWrapper}>
            {rows.map(({ label, value, note, href, external }) => (
                <a key={value} href={href} className={styles.Row} {...getLinkTargetProps(external)}>
                    <span className={styles.Text}>
                        <span className={styles.Label}>{label}</span>
                        <span className={styles.Value}>{value}</span>
                        <span className={styles.Note}>{note}</span>
                    </span>
                    <span className={styles.Arrow}>{external ? "↗" : "→"}</span>
                </a>
            ))}
        </div>
    );
};
