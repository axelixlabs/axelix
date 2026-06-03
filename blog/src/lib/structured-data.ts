import { getBaseUrl, withBlogBasePath } from "./url";
import { getAuthors } from "./authors";
import { GITHUB_URL, SITE_NAME } from "./blog-metadata";

function absolute(path: string): string {
  return new URL(withBlogBasePath(path), getBaseUrl()).toString();
}

const ORGANIZATION = {
  "@type": "Organization",
  name: SITE_NAME,
  url: getBaseUrl(),
  sameAs: [GITHUB_URL],
};

interface BlogPostingInput {
  title: string;
  description: string;
  slug: string;
  date: string;
  modified?: string;
  authors: string[];
  image?: string | null;
}

/** BlogPosting JSON-LD for a post (only when title + description exist). */
export function getBlogPostingJsonLd(input: BlogPostingInput) {
  const authors = getAuthors(input.authors).map((a) => ({ "@type": "Person", name: a.name }));
  return {
    "@context": "https://schema.org",
    "@type": "BlogPosting",
    headline: input.title,
    description: input.description,
    datePublished: input.date,
    dateModified: input.modified ?? input.date,
    url: absolute(`/${input.slug}`),
    author: authors.length === 1 ? authors[0] : authors,
    publisher: ORGANIZATION,
    ...(input.image ? { image: new URL(input.image, getBaseUrl()).toString() } : {}),
  };
}
