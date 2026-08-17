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

import { Copy, InfoTooltip } from "@/components";
import { deriveProblems, formatTransactionDuration } from "@/helpers";
import type { ITransactionAggregatedProfile } from "@/models";
import { problemClassToken, problemDescriptionKey, problemLabelKey } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The transaction whose expanded detail is rendered.
     */
    transaction: ITransactionAggregatedProfile;
}

export const TransactionDetail = ({ transaction }: IProps) => {
    const { t } = useTranslation();

    const { className, methodName } = transaction.transactionalKey;
    const { minMs, maxMs, averageMs } = transaction.transactionOverallStats;
    const accessLabel = t(transaction.readOnly ? "Transactional.readOnly" : "Transactional.readWrite");

    const problems = deriveProblems(transaction);

    return (
        <div className={styles.Detail}>
            <div className={styles.Stats}>
                <div className={styles.StatCard}>
                    <span className={styles.StatLabel}>{t("Transactional.detail.min")}</span>
                    <span className={styles.StatValue}>{formatTransactionDuration(minMs)}</span>
                </div>
                <div className={`${styles.StatCard} ${styles.StatAvg}`}>
                    <span className={styles.StatLabel}>{t("Transactional.detail.avgDuration")}</span>
                    <span className={styles.StatValue}>{formatTransactionDuration(averageMs)}</span>
                </div>
                <div className={styles.StatCard}>
                    <span className={styles.StatLabel}>{t("Transactional.detail.max")}</span>
                    <span className={styles.StatValue}>{formatTransactionDuration(maxMs)}</span>
                </div>
            </div>

            <div className={styles.Meta}>
                <div className={styles.MetaItem}>
                    <span className={styles.MetaLabel}>{t("Transactional.detail.declaredIn")}</span>
                    <span className={styles.DeclaredIn}>
                        <span className={styles.DeclaredClass}>{className}</span>
                        <span className={styles.DeclaredHash}>#</span>
                        <span className={styles.DeclaredMethod}>{methodName}()</span>
                    </span>
                    <Copy text={`${className}#${methodName}`} />
                </div>
                <div className={styles.MetaDivider} />
                <div className={styles.MetaItem}>
                    <span className={styles.MetaLabel}>{t("Transactional.detail.propagation")}</span>
                    <span className={styles.MetaBadge}>{transaction.propagation ?? "—"}</span>
                </div>
                <div className={styles.MetaItem}>
                    <span className={styles.MetaLabel}>{t("Transactional.detail.isolation")}</span>
                    <span className={styles.MetaBadge}>{transaction.isolation ?? "—"}</span>
                </div>
                <div className={styles.MetaItem}>
                    <span className={styles.MetaLabel}>{t("Transactional.detail.access")}</span>
                    <span className={styles.MetaBadge}>{accessLabel}</span>
                </div>
            </div>

            {problems.length === 0 ? (
                <div className={styles.NoProblemsCard}>{t("Transactional.detail.noProblems")}</div>
            ) : (
                problems.map((problem, index) => (
                    <div
                        key={`${problem.type}-${index}`}
                        className={`${styles.ProblemCard} ${styles[problemClassToken[problem.type]]}`}
                    >
                        <div className={styles.ProblemHeader}>
                            <span className={styles.ProblemLabel}>{t(problemLabelKey[problem.type])}</span>
                            <InfoTooltip text={t(problemDescriptionKey[problem.type])} />
                        </div>
                        <div className={styles.ProblemDetail}>
                            {problem.detail}
                            {problem.count && problem.count > 1 ? ` (×${problem.count})` : ""}
                        </div>
                    </div>
                ))
            )}
        </div>
    );
};
