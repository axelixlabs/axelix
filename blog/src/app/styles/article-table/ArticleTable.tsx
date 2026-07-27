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
import type { ReactNode } from "react";

import example from "./example.module.css";

// Registry of per-table CSS Modules. Add an entry for each styled table:
//   import pricing from "./pricing.module.css";
//   const TABLES = { example, pricing };
// Each module must expose a `.table` wrapper class.
const TABLES: Record<string, Record<string, string>> = {
    example,
};

/**
 * Wraps a Markdown table and applies a scoped per-table CSS Module by `name`.
 * Usage in MDX (keep blank lines around the table):
 *
 *   <ArticleTable name="example">
 *
 *   | … | … |
 *   | - | - |
 *
 *   </ArticleTable>
 */
export function ArticleTable({ name, children }: { name: string; children: ReactNode }) {
    return <div className={TABLES[name]?.table}>{children}</div>;
}
