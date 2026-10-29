import { notFound } from "next/navigation";
import { blog } from "@/lib/source";
import { getLLMText } from "@/lib/get-llm-text";

export const revalidate = false;

export async function GET(_request: Request, { params }: RouteContext<"/llms.mdx/blog/[slug]/content.md">) {
  const { slug } = await params;
  const page = blog.getPage([slug]);
  if (!page) notFound();

  return new Response(await getLLMText(page), {
    headers: {
      "Content-Type": "text/markdown; charset=utf-8",
      Vary: "Accept",
    },
  });
}

export function generateStaticParams() {
  return blog.getPages().map((page) => ({ slug: page.slugs[0] }));
}
