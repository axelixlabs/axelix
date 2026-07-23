import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { blog } from "@/lib/source";
import { ArticleHeader, ArticleTOC, ReadProgress, JsonLd } from "@/components";
import { withBlogBasePath } from "@/lib/url";

export function generateStaticParams() {
  return blog.getPages().map(({ slugs }) => ({ slug: slugs[0] }));
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ slug: string }>;
}): Promise<Metadata> {
  const { slug } = await params;
  const page = blog.getPage([slug]);
  if (!page) {
    return {};
  }

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

export default async function PostPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  const page = blog.getPage([slug]);

  if (!page) {
    notFound();
  }

  return (
    <>
      <ReadProgress />

      <JsonLd data={page.data} slug={slug} />

      <div className="wrap">
        <div className="art-top">
          <Link className="back-link" href="/">
            <span className="arr">←</span> Back to blog
          </Link>
        </div>

        <ArticleHeader data={page.data} />

        <ArticleTOC page={page} slug={slug} />
      </div>
    </>
  );
}
