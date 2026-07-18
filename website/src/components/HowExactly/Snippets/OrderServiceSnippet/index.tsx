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
import { CodeBlock } from "../CodeBlock";
import styles from "../shared.module.css";

export const OrderServiceSnippet = () => {
    return (
        <CodeBlock fileName="OrderService.java" tag="quiet time-ticking bomb">
            <pre className={styles.Snippet}>
                <code>
                    <span className={styles.Line}>
                        <span className={styles.Keyword}>@Transactional</span>(readOnly ={" "}
                        <span className={styles.Keyword}>true</span>)
                    </span>
                    <span className={styles.Line}>
                        <span className={styles.Keyword}>public </span> Page&lt;OrderSummary&gt; recentOrders(Filter
                        filter, Pageable page) {"{"}
                    </span>
                    <br />
                    <span className={styles.Line}>
                        {"  "}
                        <span className={styles.Keyword}>return</span>{" "}
                        orderRepository.findAll(OrderSpecs.matching(filter), page)
                    </span>
                    <span className={styles.Line}>{"    .map(order -> {"}</span>
                    <span className={styles.Line}>
                        {"      "}
                        <span className={styles.Keyword}>var</span> items = order.getLineItems();
                        {"   "}
                    </span>
                    <span className={styles.Line}>
                        {"      "}
                        <span className={styles.Keyword}>var</span> fx = pricingClient.get()
                    </span>
                    <span className={styles.Line}>{'          .uri("/fx/{ccy}", order.currency())'}</span>
                    <span className={styles.Line}>{"          .retrieve().body(FxRate.class);"}</span>
                    <span className={styles.Line}>
                        {"      "}
                        <span className={styles.Keyword}>return</span> orderMapper.toSummary(order, items, fx);
                    </span>
                    <span className={styles.Line}>{"    });"}</span>
                    <span className={styles.Line}>{"}"}</span>
                </code>
            </pre>
        </CodeBlock>
    );
};
