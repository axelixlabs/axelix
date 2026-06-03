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
import { CheckIcon, ChevronDownIcon } from "@/assets";

import styles from "./styles.module.css";

export interface IProps {
    label: string;
    open: boolean;
    onToggle: () => void;
    options: { id: string; label: string }[];
    active: string;
    onPick: (id: string) => void;
}

export const InstallerSelect = ({ label, open, onToggle, options, active, onPick }: IProps) => {
    return (
        <div className={`${styles.MainWrapper} ${open ? styles.ActiveWrapper : ""}`}>
            <button
                className={styles.Trigger}
                type="button"
                onClick={(e) => {
                    e.stopPropagation();
                    onToggle();
                }}
            >
                <span className={styles.Label}>{label}</span>
                <ChevronDownIcon className={styles.Arrow} />
            </button>
            <div className={styles.Menu}>
                {options.map(({ id, label }) => (
                    <button
                        key={id}
                        className={`${styles.Option} ${active === id ? styles.ActiveOption : ""}`}
                        type="button"
                        onClick={() => {
                            onPick(id);
                        }}
                    >
                        {label}
                        <CheckIcon className={styles.CheckIcon} />
                    </button>
                ))}
            </div>
        </div>
    );
};
