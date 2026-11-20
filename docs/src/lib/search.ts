import { createFromSource } from "fumadocs-core/search/server";

import { source } from "@/lib/source";

const processCache = globalThis as typeof globalThis & {
    axelixDocsSearch?: ReturnType<typeof createFromSource>;
};

// Next.js can bundle instrumentation and the route separately.
export const search =
    process.env.NODE_ENV === "production"
        ? (processCache.axelixDocsSearch ??= createFromSource(source))
        : createFromSource(source);
