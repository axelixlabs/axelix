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
import { EnterpriseExtensionList } from "./EnterpriseExtensionList";
import { EnterpriseFooter } from "./EnterpriseFooter";
import { EnterpriseStack } from "./EnterpriseStack";
import { EnterpriseTopSection } from "./EnterpriseTopSection";
import styles from "./styles.module.css";

export const Enterprise = () => {
    return (
        <section className={styles.MainWrapper} id="enterprise">
            <div className="wrap">
                <EnterpriseTopSection />

                <div className={styles.ContentWrapper}>
                    <div>
                        <EnterpriseStack />
                        <div className={styles.ThirdCardFooter}>Same surface · same MCP · one product</div>
                    </div>
                    <EnterpriseExtensionList />
                </div>

                <EnterpriseFooter />
            </div>
        </section>
    );
};
