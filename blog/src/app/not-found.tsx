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
import { BackLink } from "@/components";

import styles from "./styles.module.css";

export default function NotFound() {
    return (
        <>
            <div className={`MainContainer ${styles.NotFoundMainWrapper}`}>
                <h1 className={styles.NotFoundTitle}>404</h1>
                <p className={styles.NotFoundDescription}>That page wandered off. Let&apos;s get you back.</p>
                <BackLink text="Back to the blog" />
            </div>
        </>
    );
}
