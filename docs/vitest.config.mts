import { fileURLToPath } from "node:url";
import { defineConfig } from "vitest/config";

export default defineConfig({
    resolve: {
        alias: {
            "@": fileURLToPath(new URL("./src", import.meta.url)),
        },
    },
    test: {
        // `next` ships no `exports` map, so `next/server` only resolves through Vite,
        // not through Node's native ESM resolution used for externalised packages
        server: { deps: { inline: ["fumadocs-core"] } },
    },
});
