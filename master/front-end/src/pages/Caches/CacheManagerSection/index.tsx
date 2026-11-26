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
import type { ICacheData, ICachesManager } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    cacheManager: ICachesManager;
    selectedCacheName?: string;
    onSelect: (cache: ICacheData) => void;
}

export const CacheManagerSection = ({ cacheManager, selectedCacheName, onSelect }: IProps) => (
    <details open>
        <summary className={styles.GroupTitle}>
            {cacheManager.name} · {cacheManager.caches.length}
        </summary>
        <ul>
            {cacheManager.caches.map((cache) => (
                <li key={cache.name}>
                    <button
                        type="button"
                        className={styles.Row}
                        title={`${cache.name} · ${cache.target}`}
                        aria-pressed={cache.name === selectedCacheName}
                        onClick={() => onSelect(cache)}
                    >
                        <strong className={styles.Name}>{cache.name}</strong>
                        <code className={styles.Target}>{cache.target}</code>
                    </button>
                </li>
            ))}
        </ul>
    </details>
);
