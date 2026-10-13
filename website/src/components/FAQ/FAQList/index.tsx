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
"use client";
import { PlusIcon } from "@/assets";

import { useState } from "react";

import { FAQ_ITEMS } from "./items";
import styles from "./styles.module.css";

export const FAQList = () => {
    const [open, setOpen] = useState<number | null>(null);

    return (
        <ol className={styles.MainWrapper}>
            {FAQ_ITEMS.map(({ question, answer }, index) => {
                const isOpen = open === index;

                return (
                    <li key={question} className={`${styles.Accordion} ${isOpen ? styles.Open : ""}`}>
                        <button
                            className={styles.WrapperButton}
                            type="button"
                            onClick={() => setOpen(isOpen ? null : index)}
                        >
                            <span className={styles.Question}>{question}</span>
                            <span className={styles.Icon}>
                                <PlusIcon height="16" width="16" />
                            </span>
                        </button>
                        <div className={styles.Answer}>
                            <div className={styles.AccordionContentWrapper}>{answer}</div>
                        </div>
                    </li>
                );
            })}
        </ol>
    );
};
