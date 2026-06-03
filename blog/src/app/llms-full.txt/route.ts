import { getSortedPosts } from "@/lib/source";
import { getLLMText } from "@/lib/get-llm-text";

// Fully static: regenerated at build time from the posts (newest first).
export const revalidate = false;

export async function GET() {
  const scanned = await Promise.all(getSortedPosts().map(getLLMText));
  return new Response(scanned.join("\n\n"), {
    headers: { "Content-Type": "text/plain; charset=utf-8" },
  });
}
