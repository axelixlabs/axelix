import { createFromSource } from "fumadocs-core/search/server";
import { blog } from "@/lib/source";

// Orama static search over every post's structuredData (headings + body).
// Served at /blog/api/search because of the app's basePath.
export const { GET } = createFromSource(blog);
