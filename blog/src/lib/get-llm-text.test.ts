import { describe, expect, it } from "vitest";
import { getLLMText } from "./get-llm-text";
import type { BlogPage } from "./source";

function post(data: { description?: string; tags?: string[] }): BlogPage {
  return {
    slugs: ["sample-post"],
    data: {
      title: "Sample post",
      authors: ["Ada Lovelace", "Grace Hopper"],
      date: new Date("2026-10-13T00:00:00Z"),
      ...data,
      getText: async () => "## Body\n\nProcessed content.",
    },
  } as unknown as BlogPage;
}

describe("getLLMText", () => {
  it("includes the post metadata before the processed Markdown", async () => {
    expect(await getLLMText(post({ description: "A short summary.", tags: ["Java", "Spring"] }))).toBe(
      `# Sample post (https://axelix.io/blog/sample-post)

> A short summary.

- **Authors:** Ada Lovelace, Grace Hopper
- **Published:** 2026-10-13
- **Tags:** Java, Spring

## Body

Processed content.`,
    );
  });

  it("omits missing description and tags", async () => {
    expect(await getLLMText(post({}))).toBe(
      `# Sample post (https://axelix.io/blog/sample-post)

- **Authors:** Ada Lovelace, Grace Hopper
- **Published:** 2026-10-13

## Body

Processed content.`,
    );
  });
});
