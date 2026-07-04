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
import { DashboardGCDistributionChart } from "./DashboardGCDistributionChart";
import { DashboardGCLoggingGauge } from "./DashboardGCLoggingGauge";
import { DashboardLeydenChart } from "./DashboardLeydenChart";
import styles from "./styles.module.css";

const DashboardJava = () => {
    return (
        <>
            <div className={styles.HeaderWrapper}>
                <div className="TextLarge">Java</div>
                <p className={styles.Subtitle}>Real-time JVM insights · Project Leyden · Garbage Collection</p>
            </div>

            <div className={styles.ChartsWrapper}>
                <DashboardLeydenChart />
                <DashboardGCDistributionChart />
                <DashboardGCLoggingGauge />
            </div>
        </>
    );
};

export default DashboardJava;
