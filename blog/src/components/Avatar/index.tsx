"use client";

import { type CSSProperties, useState } from "react";
import { authorImageCandidates, getAuthor } from "@/lib/authors";
import { withBlogBasePathForImageSrc } from "@/lib/url";

interface IProps {
  /** Author display name. */
  authorRef: string;
  /** Override the default 26px diameter (e.g. 64 on cards, 120 on profiles).
   *  Inline width/height/font-size win over the shared `.avatar` class. */
  size?: number;
}

/** Author avatar: tries the convention photos `public/authors/<slug>.{png,jpg,jpeg,svg}`
 *  in order, falling back to an initials circle once none load (no broken images). */
export const Avatar = ({ authorRef, size }: IProps) => {
  const a = getAuthor(authorRef);
  const candidates = authorImageCandidates(a.slug);
  const [idx, setIdx] = useState(0);

  const sized: CSSProperties | undefined = size
    ? { width: size, height: size, fontSize: Math.round(size * 0.28) }
    : undefined;

  if (idx >= candidates.length) {
    return (
      <span className="avatar" style={{ background: a.color, ...sized }}>
        {a.initials}
      </span>
    );
  }

  const advance = () => setIdx((i) => i + 1);

  return (
    // `key` gives each candidate a fresh element so its onError handler is
    // attached before the new src loads. The ref covers the SSR case: React
    // won't re-fire onError for an <img> that already failed before hydration,
    // so detect the already-broken image on mount and advance.
    // eslint-disable-next-line @next/next/no-img-element
    <img
      key={idx}
      className="avatar"
      style={sized}
      src={withBlogBasePathForImageSrc(candidates[idx])}
      alt={a.name}
      ref={(node) => {
        if (node && node.complete && node.naturalWidth === 0) advance();
      }}
      onError={advance}
    />
  );
};
