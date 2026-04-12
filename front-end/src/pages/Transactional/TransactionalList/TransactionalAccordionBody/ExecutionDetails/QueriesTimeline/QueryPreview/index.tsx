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
import { useTranslation } from "react-i18next";

import { toFormattedTimeWithMs } from "helpers";
import type { IQueryData } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The query executed during a particular transaction.
     */
    query: IQueryData;
}

export const QueryPreview = ({ query }: IProps) => {
    const { t } = useTranslation();

    const { startTimestampMs, endTimestampMs } = query;
    const durationMs = endTimestampMs - startTimestampMs;

    return (
        <div className={styles.MainWrapper}>
            <div>{t("Transactional.previewDuration", { durationMs })}</div>
            <div>Timestamp : {toFormattedTimeWithMs(startTimestampMs)}</div>
        </div>
    );
};
