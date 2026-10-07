/** An author is just a **name** in a post's frontmatter (`authors: ["Jane Doe"]`).
 *  Everything else is derived from the name — there is no registry:
 *  - the avatar is a convention path `public/authors/<slug>.png` (drop a PNG to
 *    add a photo); when the file is missing the UI falls back to an initials
 *    circle (see `Avatar`).
 *  - the slug derivation mirrors the reference blog (Prisma). */

export interface Author {
  name: string;
  /** URL-safe id derived from the name; also the avatar filename. */
  slug: string;
  /** Fallback circle text when the photo is missing. */
  initials: string;
  /** Fallback circle color (deterministic from the name). */
  color: string;
}

/** Avatar image formats tried, in order, for `public/authors/<slug>.<ext>`. */
export const AUTHOR_IMAGE_EXTENSIONS = ["png", "jpg", "jpeg", "svg"] as const;

/** Convention avatar paths to try in order; the first that loads wins (see `Avatar`). */
export function authorImageCandidates(slug: string): string[] {
  return AUTHOR_IMAGE_EXTENSIONS.map((ext) => `/authors/${slug}.${ext}`);
}

const FALLBACK_COLORS = ["#639922", "#2A6FDB", "#7c5cd6", "#1f8f6a", "#c98a1f"];

function hash(str: string): number {
  let h = 0;
  for (let i = 0; i < str.length; i += 1) h = (h * 31 + str.charCodeAt(i)) >>> 0;
  return h;
}

function initialsFromName(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "?";
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

/** Normalize a display name to a comparable, ascii-ish key (mirrors Prisma). */
function normalizeAuthorName(name: string): string {
  const latinized = name
    .replace(/[øØ]/g, "o")
    .replace(/[æÆ]/g, "ae")
    .replace(/[œŒ]/g, "oe")
    .replace(/[ß]/g, "ss");

  return latinized
    .normalize("NFD")
    .replace(/[̀-ͯ]/g, "") // strip combining diacritics
    .replace(/['‘’]/g, "") // drop apostrophes
    .replace(/[^a-zA-Z0-9\s-]/g, " ")
    .trim()
    .toLowerCase()
    .replace(/\s+/g, " ");
}

function toAuthorSlug(name: string): string {
  return normalizeAuthorName(name).replace(/\s+/g, "-");
}

/** Resolve a display name to a full {@link Author} (all fields derived). */
export function getAuthor(name: string): Author {
  const slug = toAuthorSlug(name);
  return {
    name,
    slug,
    initials: initialsFromName(name),
    color: FALLBACK_COLORS[hash(slug || name) % FALLBACK_COLORS.length],
  };
}

/** Resolve a list of names, de-duplicated by normalized name. */
export function getAuthors(names: string[]): Author[] {
  const seen = new Set<string>();
  return names
    .filter(Boolean)
    .map((name) => name.trim())
    .filter((name) => {
      const normalized = normalizeAuthorName(name);
      if (seen.has(normalized)) return false;
      seen.add(normalized);
      return true;
    })
    .map(getAuthor);
}

/** A social/profile link shown on an author's profile page. `kind` selects the
 *  icon (see `AUTHOR_LINK_ICONS` in the profile page). */
export interface AuthorLink {
  kind: "github" | "x" | "linkedin" | "rss";
  label: string;
  url: string;
}

/** The only author facts that can't be derived from a name + their posts: an
 *  editorial role, bio, and social links. Everything else (avatar, slug,
 *  article count, topics, dates, reading time) stays derived. */
export interface AuthorProfile {
  /** Role/affiliation shown under the name, e.g. "Core team · Axelix Labs". */
  title: string;
  bio: string;
  links: AuthorLink[];
}

/** Editorial profiles, keyed by author slug. An author without an entry still
 *  gets a profile page — just without the bio/role/links. */
const AUTHOR_PROFILES: Record<string, AuthorProfile> = {
  "mikhail-polivakha": {
    title: "Founder & CEO of Axelix Labs",
    bio: "Founder & CEO of Axelix Labs. Programming nerd & Software Engineer. Dedicated to Java & Spring Boot. Contributor to various OSS projects, including Spring Data & Spring Boot. Enthusiast of Open Source & public speaker on Spring I/O and Devoxx.",
    links: [
      { kind: "github", label: "GitHub", url: "https://github.com/mipo256" },
      { kind: "x", label: "X", url: "https://x.com/mpolivaha" },
      { kind: "linkedin", label: "LinkedIn", url: "https://www.linkedin.com/in/mikhail-polivakha-7995571ab/" },
    ],
  },
  "dmitry-mazurov": {
    title: "Senior Software Engineer",
    bio: "Contributor to Axelix OSS. Backend engineer working with Java and Spring Boot. Interested in everything around the code: architecture, security, infrastructure, and automation. Likes understanding how the whole system works, not just a single component.",
    links: [
      { kind: "github", label: "GitHub", url: "https://github.com/dima-bzz" },
    ],
  },
  "sergey-cherkasov": {
    title: "Developer Advocate · Axelix Core Team",
    bio: "Developer Advocate in the Axelix Core Team. Actively involved in development of the Axelix's core.",
    links: [
      { kind: "github", label: "GitHub", url: "https://github.com/sergeycherkasovv" },
      { kind: "x", label: "X", url: "https://x.com/iamcherkasov" },
      { kind: "linkedin", label: "LinkedIn", url: "https://www.linkedin.com/in/sergei-cherkasov-968721210" },
    ],
  },
};

/** The editorial profile for an author slug, or `null` if there isn't one. */
export function getAuthorProfile(slug: string): AuthorProfile | null {
  return AUTHOR_PROFILES[slug] ?? null;
}
