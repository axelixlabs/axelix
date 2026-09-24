import { remarkDirectiveAdmonition } from "fumadocs-core/mdx-plugins";
import { defineConfig } from "fumadocs-mdx/config";
import remarkDirective from "remark-directive";

/**
 * Collections live in `src/lib/source.ts` via the `defineDocs` macro — this file only carries
 * the global MDX options, which the macro has no place for.
 */
export default defineConfig({
    mdxOptions: {
        // A function extends the `fumadocs` preset; an array would replace it.
        remarkPlugins: (plugins) => [
            remarkDirective,
            [
                remarkDirectiveAdmonition,
                {
                    // Replaces the plugin's own map, so every `:::type` the articles use must be
                    // listed — an unlisted one renders as plain text instead of a callout.
                    types: {
                        note: "info",
                        info: "info",
                        important: "info",
                        // Docusaurus draws `:::tip` with a lightbulb; `idea` is the Fumadocs
                        // callout that carries one.
                        tip: "idea",
                        warning: "warning",
                    },
                },
            ],
            ...plugins,
        ],
    },
});
