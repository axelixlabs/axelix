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
import type { PropsWithChildren } from "react";
import { useTranslation } from "react-i18next";

import type { IDetailsCardRecord } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Details card title
     */
    title: string;

    /**
     * Prefix to be used when translating {@link records} via i18n.
     */
    i18nPropertiesPrefix: string;

    /**
     * Details card records
     */
    records: IDetailsCardRecord[];
}

export const DetailsCard = ({ children, i18nPropertiesPrefix, title, records }: PropsWithChildren<IProps>) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={`CustomizedTable ${styles.Card}`}>
                <div className="TableHeader">
                    <div className={`RowChunk ${styles.TableHeaderRowChunk}`}>
                        {children}
                        {t(title)}
                    </div>
                </div>

                {records.map(({ key, value }) => (
                    <div className="TableRow" key={key}>
                        <div className="RowChunk">{t(`${i18nPropertiesPrefix}.${key}`)}</div>
                        <div className={`RowChunk ${styles.ValueChunk}`}>
                            <div className={styles.ValueWrapper}>{value}</div>
                        </div>
                    </div>
                ))}
            </div>
        </>
    );
};
