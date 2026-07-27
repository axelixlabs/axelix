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
