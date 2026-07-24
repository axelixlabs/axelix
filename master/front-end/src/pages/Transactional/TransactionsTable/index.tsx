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

import type { ITransactionAggregatedProfile } from "models";

import { TransactionRow } from "./TransactionRow";
import styles from "./styles.module.css";

interface IProps {
    /**
     * The transactions to display, already filtered.
     */
    transactions: ITransactionAggregatedProfile[];
}

export const TransactionsTable = ({ transactions }: IProps) => {
    const { t } = useTranslation();

    if (transactions.length === 0) {
        return <div className={styles.Empty}>{t("Transactional.noMatches")}</div>;
    }

    return (
        <div className={styles.Scroll}>
            <div className={styles.Table}>
                <div className={styles.HeaderRow}>
                    <span>{t("Transactional.columns.transaction")}</span>
                    <span>{t("Transactional.columns.access")}</span>
                    <span>{t("Transactional.columns.avgTime")}</span>
                    <span>{t("Transactional.columns.problems")}</span>
                    <span />
                </div>
                {transactions.map((transaction) => (
                    <TransactionRow
                        key={`${transaction.transactionalKey.className}#${transaction.transactionalKey.methodName}`}
                        transaction={transaction}
                    />
                ))}
            </div>
        </div>
    );
};
