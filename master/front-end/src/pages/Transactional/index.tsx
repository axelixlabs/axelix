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
import { useEffect, useState } from "react";
import { useParams } from "react-router";

import { EmptyHandler, Loader, PageSearch } from "components";
import { fetchData, filterTransactions, isProblematic } from "helpers";
import { type EProblemType, type IPersistenceInsights, StatefulRequest } from "models";
import { getTransactionalData } from "services";

import { CleanState } from "./CleanState";
import { ProblemTypeFilter } from "./ProblemTypeFilter";
import { TransactionControlSummary } from "./TransactionControlSummary";
import { TransactionsTable } from "./TransactionsTable";
import styles from "./styles.module.css";

const Transactional = () => {
    const [insights, setInsights] = useState(StatefulRequest.loading<IPersistenceInsights>());
    const [search, setSearch] = useState<string>("");
    const [activeProblemTypes, setActiveProblemTypes] = useState<EProblemType[]>([]);

    const { instanceId } = useParams();

    useEffect(() => {
        fetchData(setInsights, () => getTransactionalData(instanceId!));
    }, []);

    if (insights.loading) {
        return <Loader />;
    }

    if (insights.error) {
        return <EmptyHandler isEmpty />;
    }

    const transactions = insights.response!.transactions ?? [];
    const analyzed = transactions.length;
    const problematic = transactions.filter(isProblematic).length;

    if (analyzed === 0) {
        return <EmptyHandler isEmpty />;
    }

    if (problematic === 0) {
        return (
            <div className={styles.Page}>
                <CleanState analyzed={analyzed} />
            </div>
        );
    }

    const toggleProblemType = (type: EProblemType): void => {
        setActiveProblemTypes((prev) =>
            prev.includes(type) ? prev.filter((current) => current !== type) : [...prev, type],
        );
    };

    const filtered = filterTransactions(transactions, search, activeProblemTypes);
    const addonAfter = `${filtered.length} / ${analyzed}`;

    return (
        <div className={styles.Page}>
            <TransactionControlSummary analyzed={analyzed} problematic={problematic} clean={analyzed - problematic} />
            <div className={styles.Toolbar}>
                <PageSearch setSearch={setSearch} addonAfter={addonAfter} removeBottomGutter />
                <ProblemTypeFilter activeProblemTypes={activeProblemTypes} onToggle={toggleProblemType} />
            </div>
            <TransactionsTable transactions={filtered} />
        </div>
    );
};

export default Transactional;
