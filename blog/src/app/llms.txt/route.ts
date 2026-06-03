import { getSortedPosts } from "@/lib/source";
import { getBaseUrl, withBlogBasePath } from "@/lib/url";
import { BLOG_HOME_DESCRIPTION, BLOG_HOME_TITLE } from "@/lib/blog-metadata";

export const revalidate = false;

export function GET() {
  const base = getBaseUrl();
  const abs = (path: string) => new URL(withBlogBasePath(path), base).toString();

  const posts = getSortedPosts()
    .map((page) => {
      const link = abs(`/${page.slugs.join("/")}`);
      const description = page.data.description ?? page.data.metaDescription ?? "";
      return `- [${page.data.title}](${link})${description ? `: ${description}` : ""}`;
    })
    .join("\n");

  const intro = BLOG_HOME_DESCRIPTION ? `\n> ${BLOG_HOME_DESCRIPTION}\n` : "";

  const text = `# ${BLOG_HOME_TITLE}
${intro}
## Posts

${posts}

## Full content

- [llms-full.txt](${abs("/llms-full.txt")})
`;

  return new Response(text, {
    headers: { "Content-Type": "text/plain; charset=utf-8" },
  });
}
