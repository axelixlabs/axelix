import type { BlogPage } from "./source";
import { getBaseUrl, withBlogBasePath } from "./url";

/** Render a post's metadata and processed Markdown for per-post and full LLM exports. */
export async function getLLMText(page: BlogPage): Promise<string> {
  const processed = await page.data.getText("processed");
  const url = new URL(withBlogBasePath(`/${page.slugs.join("/")}`), getBaseUrl()).toString();
  const metadata = [
    ...(page.data.description ? [`> ${page.data.description}`, ""] : []),
    `- **Authors:** ${page.data.authors.join(", ")}`,
    `- **Published:** ${page.data.date.toISOString().slice(0, 10)}`,
    ...(page.data.tags?.length ? [`- **Tags:** ${page.data.tags.join(", ")}`] : []),
  ].join("\n");

  return `# ${page.data.title} (${url})\n\n${metadata}\n\n${processed}`;
}
