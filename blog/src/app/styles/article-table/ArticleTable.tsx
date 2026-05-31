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
