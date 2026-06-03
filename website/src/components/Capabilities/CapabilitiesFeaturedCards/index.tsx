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
import { BeansIcon, ConfigIcon, LoggersIcon, TransactionsIcon, VisGraphIcon } from "@/assets";

import { AccessBadge } from "../AccessBadge";
import sharedStyles from "../shared.module.css";

import styles from "./styles.module.css";

const featuredCards = [
    {
        title: "Transactional inspection",
        description: "Live transactions, durations, the SQL timeline of each one — the verbs your APM forgot.",
        icon: <TransactionsIcon width="22" height="22" />,
        href: "https://axelix.io/docs/features/transaction-control",
        visual: (
            <div className={styles.VisualBars}>
                <span className={styles.VisualBar} style={{ height: "30%" }} />
                <span className={styles.VisualBar} style={{ height: "55%" }} />
                <span className={styles.VisualBar} style={{ height: "40%" }} />
                <span className={`${styles.VisualBar} ${styles.Highlighted}`} style={{ height: "92%" }} />
                <span className={styles.VisualBar} style={{ height: "48%" }} />
                <span className={styles.VisualBar} style={{ height: "62%" }} />
                <span className={styles.VisualBar} style={{ height: "38%" }} />
                <span className={styles.VisualBar} style={{ height: "70%" }} />
                <span className={styles.VisualBar} style={{ height: "44%" }} />
                <span className={`${styles.VisualBar} ${styles.Highlighted}`} style={{ height: "84%" }} />
                <span className={styles.VisualBar} style={{ height: "50%" }} />
                <span className={styles.VisualBar} style={{ height: "22%" }} />
            </div>
        ),
    },
    {
        title: "Configuration properties",
        description: (
            <>
                Effective values for every <code className={styles.CodeInline}>@Value</code> and bound prefix — and
                where they actually came from.
            </>
        ),
        icon: <ConfigIcon width="22" height="22" />,
        href: "https://axelix.io/docs/features/configuration-properties",
        visual: (
            <div className={styles.PropsTable}>
                <div className={styles.Row}>
                    <span>datasource.url</span>
                    <span className={styles.RowValue}>vault</span>
                </div>
                <div className={styles.Row}>
                    <span>cache.ttl</span>
                    <span className={styles.RowValue}>30s</span>
                </div>
                <div className={styles.Row}>
                    <span>retry.max</span>
                    <span className={styles.RowValue}>env</span>
                </div>
                <div className={styles.Row}>
                    <span>feature.x</span>
                    <span className={`${styles.RowValue} ${styles.BlueText}`}>on</span>
                </div>
            </div>
        ),
    },
    {
        title: "Loggers",
        description: "Flip log levels per package, live — no redeploy, no SSH, fully audited.",
        icon: <LoggersIcon width="22" height="22" />,
        href: "https://axelix.io/docs/features/loggers",
        visual: (
            <div className={styles.LoggersTree}>
                <div className={styles.Row}>
                    <span className={styles.LoggersTreeKey}>org.hibernate.SQL</span>
                    <span className={`${styles.Level} ${styles.Trace}`}>trace</span>
                </div>
                <div className={styles.Row}>
                    <span className={styles.LoggersTreeKey}>o.s.web</span>
                    <span className={`${styles.Level} ${styles.Info}`}>info</span>
                </div>
                <div className={styles.Row}>
                    <span className={styles.LoggersTreeKey}>com.acme.svc</span>
                    <span className={`${styles.Level} ${styles.Debug}`}>debug</span>
                </div>
            </div>
        ),
    },
    {
        title: "Beans & conditions",
        description: (
            <>
                The live bean graph plus the <code className={styles.CodeInline}>@Conditional</code> verdicts — why each
                bean is here, or isn&apos;t.
            </>
        ),
        icon: <BeansIcon width="22" height="22" />,
        href: "https://axelix.io/docs/features/beans",
        visual: <VisGraphIcon className={styles.Graph} />,
    },
];

export const CapabilitiesFeaturedCards = () => {
    return (
        <div className={styles.MainWrapper}>
            {featuredCards.map(({ title, icon, description, visual, href }) => (
                <a rel="noopener noreferrer" target="_blank" href={href} key={title}>
                    <article key={title} className={`${sharedStyles.Card} ${styles.Card}`}>
                        <header className={sharedStyles.CardHeader}>
                            <div className={styles.Icon}>{icon}</div>
                            <AccessBadge />
                        </header>
                        <div>
                            <h3 className={styles.Title}>{title}</h3>
                            <p className={styles.Description}>{description}</p>
                        </div>
                        <div className={styles.Visual}>{visual}</div>
                    </article>
                </a>
            ))}
        </div>
    );
};
