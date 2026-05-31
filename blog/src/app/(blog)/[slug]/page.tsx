import React from "react";
import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { createRelativeLink } from "fumadocs-ui/mdx";
import { TOCProvider, TOCScrollArea } from "fumadocs-ui/components/toc";
import { TOCItems, TOCItem } from "fumadocs-ui/components/toc/default";
import { blog } from "@/lib/source";
import { getMDXComponents } from "@/mdx-components";
import { Authors, PlainTag, ReadingTime, ReadProgress, BlogShare } from "@/components";
import { formatDate } from "@/lib/format";
import { computeReadingTime } from "@/lib/reading-time";
import { getBaseUrl, withBlogBasePath } from "@/lib/url";
import { getBlogPostingJsonLd } from "@/lib/structured-data";

export function generateStaticParams() {
  return blog.getPages().map((page) => ({ slug: page.slugs[0] }));
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ slug: string }>;
}): Promise<Metadata> {
  const { slug } = await params;
  const page = blog.getPage([slug]);
  if (!page) return {};

  const title = page.data.metaTitle ?? page.data.title;
  const description = page.data.metaDescription ?? page.data.description ?? "";
  const canonical = withBlogBasePath(`/${slug}`);

  return {
    title,
    description,
    alternates: { canonical },
    openGraph: { title, description, url: canonical, type: "article" },
    twitter: { card: "summary_large_image", title, description },
  };
}

/** Recursively extracts the text of a heading's children (for the slug id). */
function extractText(node: React.ReactNode): string {
  if (typeof node === "string") return node;
  if (typeof node === "number") return String(node);
  if (Array.isArray(node)) return node.map(extractText).join("");
  if (React.isValidElement(node))
    return extractText((node.props as { children?: React.ReactNode }).children);
  return "";
}

export default async function PostPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  const page = blog.getPage([slug]);
  if (!page) notFound();

  const MDX = page.data.body;
  const raw = await page.data.getText("raw");
  const readingMinutes = computeReadingTime(raw);
  const canonical = new URL(withBlogBasePath(`/${slug}`), getBaseUrl()).toString();

  const description = page.data.metaDescription ?? page.data.description ?? "";
  const jsonLd = getBlogPostingJsonLd({
    title: page.data.title,
    description,
    slug,
    date: page.data.date.toISOString(),
    authors: page.data.authors,
  });
  // Escape characters that could break out of the <script> block — e.g. an
  // author name or title containing `</script>`.
  const jsonLdHtml = JSON.stringify(jsonLd)
    .replace(/</g, "\\u003c")
    .replace(/>/g, "\\u003e")
    .replace(/&/g, "\\u0026");

  return (
    <>
      <ReadProgress />
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: jsonLdHtml }}
      />
      <div className="wrap">
        <div className="art-top">
          <Link className="back-link" href="/">
            <span className="arr">←</span> Back to blog
          </Link>
        </div>

        <header className="art-hero">
          <h1>{page.data.title}</h1>
          {page.data.description && <p className="standfirst">{page.data.description}</p>}
          <div className="art-meta">
            <Authors authors={page.data.authors} />
            <span className="sep" />
            <span className="m">{formatDate(page.data.date)}</span>
            <span className="sep" />
            <ReadingTime minutes={readingMinutes} className="m" />
          </div>
          <div className="rtags">
            {(page.data.tags ?? []).map((tag) => (
              <PlainTag key={tag} label={tag} href={`/?tag=${encodeURIComponent(tag)}`} />
            ))}
          </div>
        </header>

        <TOCProvider toc={page.data.toc}>
          <div className="art-layout">
          <article className="prose">
            <MDX
              components={getMDXComponents({
                a: createRelativeLink(blog, page),
                h2: (props) => {
                  const providedId =
                    typeof (props as { id?: unknown }).id === "string"
                      ? ((props as { id?: string }).id ?? "")
                      : "";
                  const id =
                    providedId ||
                    extractText(props.children)
                      .trim()
                      .toLowerCase()
                      .replace(/\s+/g, "-")
                      .replace(/[^a-z0-9-]/g, "")
                      .replace(/-+/g, "-")
                      .replace(/^-|-$/g, "");
                  return (
                    <h2 id={id} className="group flex scroll-mt-28 items-center gap-2">
                      <a href={`#${id}`}>{props.children}</a>
                    </h2>
                  );
                },
              })}
            />
            <div className="art-foot">
              <Link className="back-link" href="/">
                <span className="arr">←</span> All articles
              </Link>
              <BlogShare url={canonical} title={page.data.title} />
            </div>
          </article>
          <aside className="toc max-md:hidden">
            <span className="toc-title">On this page</span>
            <TOCScrollArea>
              <TOCItems>
                {page.data.toc.map((item) => (
                  <TOCItem key={item.url} item={item} />
                ))}
              </TOCItems>
            </TOCScrollArea>
          </aside>
          </div>
        </TOCProvider>
      </div>
    </>
  );
}
