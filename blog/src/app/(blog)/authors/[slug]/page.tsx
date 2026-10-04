import { type CSSProperties, Suspense } from "react";
import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { AuthorPosts, Avatar } from "@/components";
import { getAuthorPageData, getAuthorSummaries } from "@/lib/source";
import type { AuthorLink } from "@/lib/authors";
import { formatDate } from "@/lib/format";
import { colorForTag } from "@/lib/tags";
import { withBlogBasePath } from "@/lib/url";
import styles from "./page.module.css";

// Fully static: params come from generateStaticParams, the `?tag=` filter is
// read client-side inside <AuthorPosts> (Suspense) so this stays prerendered.
export const revalidate = false;

export async function generateStaticParams() {
  const authors = await getAuthorSummaries();
  return authors.map((author) => ({ slug: author.slug }));
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ slug: string }>;
}): Promise<Metadata> {
  const { slug } = await params;
  const data = await getAuthorPageData(slug);
  if (!data) return {};

  const { summary, profile } = data;
  const description = profile?.bio ?? `Articles by ${summary.name} on the Axelix blog.`;
  const canonical = withBlogBasePath(`/authors/${slug}`);

  return {
    title: summary.name,
    description,
    alternates: { canonical },
    openGraph: { title: summary.name, description, url: canonical, type: "profile" },
  };
}

/** Icon `d` for each social link kind (tabler-style, 24×24, stroke). */
const AUTHOR_LINK_ICONS: Record<AuthorLink["kind"], string> = {
  github:
    "M9 19c-4.3 1.4 -4.3 -2.5 -6 -3m12 5v-3.5c0 -1 .1 -1.4 -.5 -2c2.8 -.3 5.5 -1.4 5.5 -6a4.6 4.6 0 0 0 -1.3 -3.2a4.2 4.2 0 0 0 -.1 -3.2s-1.1 -.3 -3.5 1.3a12.3 12.3 0 0 0 -6.2 0c-2.4 -1.6 -3.5 -1.3 -3.5 -1.3a4.2 4.2 0 0 0 -.1 3.2a4.6 4.6 0 0 0 -1.3 3.2c0 4.6 2.7 5.7 5.5 6c-.6 .6 -.6 1.2 -.5 2v3.5",
  x: "M4 4l11.733 16h4.267l-11.733 -16z M4 20l6.768 -6.768m2.46 -2.46l6.772 -6.772",
  linkedin:
    "M8 11v5 M8 8v.01 M12 16v-5 M16 16v-3a2 2 0 0 0 -4 0 M3 7a4 4 0 0 1 4 -4h10a4 4 0 0 1 4 4v10a4 4 0 0 1 -4 4h-10a4 4 0 0 1 -4 -4z",
  rss: "M4 19a1 1 0 1 0 2 0a1 1 0 1 0 -2 0 M4 4a16 16 0 0 1 16 16 M4 11a9 9 0 0 1 9 9",
};

/** Aside topic chip: reuses the global `.tag` look but forces a white fill
 *  (the aside sits on a green gradient) and carries the tag color via `--cat`. */
const topicStyle = (name: string): CSSProperties => ({
  background: "var(--paper)",
  ["--cat" as string]: colorForTag(name),
});

function ProfileLink({ link }: { link: AuthorLink }) {
  const external = /^https?:\/\//.test(link.url);
  return (
    <a
      className={styles.Link}
      href={link.url}
      {...(external ? { target: "_blank", rel: "noopener noreferrer" } : {})}
    >
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.7"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <path d={AUTHOR_LINK_ICONS[link.kind]} />
      </svg>
      {link.label}
    </a>
  );
}

export default async function AuthorProfilePage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const data = await getAuthorPageData(slug);
  if (!data) notFound();

  const { summary, profile, posts } = data;
  const firstName = summary.name.split(" ")[0];

  return (
    <div className="wrap">
      <nav className={styles.Crumb} aria-label="Breadcrumb">
        <Link href="/">Blog</Link>
        <span className={styles.Sep}>/</span>
        <Link href="/authors">Authors</Link>
        <span className={styles.Sep}>/</span>
        <span className={styles.Current}>{summary.name}</span>
      </nav>

      <header className={styles.Header}>
        <div className={styles.Ident}>
          <div className={styles.NameRow}>
            <span className={styles.AvatarRing}>
              <Avatar authorRef={summary.name} size={120} />
            </span>
            <div className={styles.NameCol}>
              <h1>{summary.name}</h1>
              {summary.title && <div className={styles.Role}>{summary.title}</div>}
            </div>
          </div>
          {summary.bio && <p className={styles.Bio}>{summary.bio}</p>}
          {profile && profile.links.length > 0 && (
            <div className={styles.Links}>
              {profile.links.map((link) => (
                <ProfileLink key={link.kind} link={link} />
              ))}
            </div>
          )}
        </div>

        <aside className={styles.Aside}>
          <div className={styles.Stats}>
            <div>
              <div className={styles.StatLabel}>Articles</div>
              <div className={styles.StatValue}>{summary.count}</div>
            </div>
            <div>
              <div className={styles.StatLabel}>Total read time</div>
              <div className={styles.StatValue}>
                {summary.totalMinutes}
                <span className={styles.StatUnit}> min</span>
              </div>
            </div>
          </div>
          <div className={styles.Meta}>
            <div className={styles.MetaRow}>
              <span className={styles.MetaKey}>Writing since</span>
              <span className={styles.MetaVal}>
                {summary.firstDate ? formatDate(summary.firstDate) : "—"}
              </span>
            </div>
            <div className={styles.MetaRow}>
              <span className={styles.MetaKey}>Latest article</span>
              <span className={styles.MetaVal}>
                {summary.latestDate ? formatDate(summary.latestDate) : "—"}
              </span>
            </div>
          </div>
          {summary.topics.length > 0 && (
            <div className={styles.Topics}>
              <div className={styles.TopicsLabel}>Writes about</div>
              <div className={styles.TopicList}>
                {summary.topics.map((topic) => (
                  <span key={topic.name} className="tag" style={topicStyle(topic.name)}>
                    {topic.name}
                    <span className={styles.TopicN}>{topic.count}</span>
                  </span>
                ))}
              </div>
            </div>
          )}
        </aside>
      </header>

      <section className={styles.Section}>
        <Suspense>
          <AuthorPosts posts={posts} firstName={firstName} />
        </Suspense>
        <div className={styles.SectionFoot}>
          <Link href="/authors" className={styles.FootLink}>
            ← All authors
          </Link>
          <Link href="/" className={styles.FootLink}>
            All articles →
          </Link>
        </div>
      </section>
    </div>
  );
}
