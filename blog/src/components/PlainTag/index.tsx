import type { CSSProperties } from "react";
import Link from "next/link";
import { colorForTag } from "@/lib/tags";

interface IProps {
  label: string;
  href?: string;
}

/** A tag chip. With `href` it renders as a link (e.g. to the filtered home);
 *  without one it stays a plain span (safe inside card-level links). */
export const PlainTag = ({ label, href }: IProps) => {
  const style: CSSProperties = { ["--cat" as string]: colorForTag(label) };
  if (href) {
    return (
      <Link href={href} className="tag tag-link" style={style}>
        {label}
      </Link>
    );
  }
  return (
    <span className="tag" style={style}>
      {label}
    </span>
  );
};
