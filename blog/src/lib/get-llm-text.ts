import type { BlogPage } from "@/lib/source";
import { getBaseUrl, withBlogBasePath } from "@/lib/url";

/** Convert a blog post to LLM-friendly markdown: an `# Title (absolute-url)`
 *  heading followed by the processed markdown body (needs
 *  `includeProcessedMarkdown: true` in source.config.ts). */
export async function getLLMText(page: BlogPage): Promise<string> {
  const processed = await page.data.getText("processed");
  const url = new URL(withBlogBasePath(`/${page.slugs.join("/")}`), getBaseUrl()).toString();
  return `# ${page.data.title} (${url})\n\n${processed}`;
}
