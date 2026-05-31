"use client";

import { useState } from "react";
import { authorImageCandidates, getAuthor } from "@/lib/authors";
import { withBlogBasePathForImageSrc } from "@/lib/url";

interface IProps {
  /** Author display name. */
  authorRef: string;
}

/** Author avatar: tries the convention photos `public/authors/<slug>.{png,jpg,jpeg,svg}`
 *  in order, falling back to an initials circle once none load (no broken images). */
export const Avatar = ({ authorRef }: IProps) => {
  const a = getAuthor(authorRef);
  const candidates = authorImageCandidates(a.slug);
  const [idx, setIdx] = useState(0);

  if (idx >= candidates.length) {
    return (
      <span className="avatar" style={{ background: a.color }}>
        {a.initials}
      </span>
    );
  }

  return (
    // eslint-disable-next-line @next/next/no-img-element
    <img
      className="avatar"
      src={withBlogBasePathForImageSrc(candidates[idx])}
      alt={a.name}
      onError={() => setIdx((i) => i + 1)}
    />
  );
};
