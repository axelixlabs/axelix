import Link from "next/link";
import { getAuthors } from "@/lib/authors";
import { Avatar } from "../Avatar";

interface IProps {
  authors: string[];
  /** Link the byline (avatar + name) to the author's profile page. Opt-in
   *  because this component is also rendered inside the feed's card/row links,
   *  where a nested <a> would be invalid HTML. Links only for a single author
   *  (every post currently has one); multi-author bylines stay plain. */
  linked?: boolean;
}

/** One or many authors — stacked avatars + a summarized name label. */
export const Authors = ({ authors, linked }: IProps) => {
  const list = getAuthors(authors);
  const names = list.map((a) => a.name);
  const label =
    names.length === 1
      ? names[0]
      : names.length === 2
        ? `${names[0]}, ${names[1]}`
        : `${names[0]} +${names.length - 1}`;

  const inner = (
    <>
      <span className="avatars">
        {list.slice(0, 3).map((a) => (
          <Avatar key={a.slug} authorRef={a.name} />
        ))}
      </span>
      <span className="who">{label}</span>
    </>
  );

  if (linked && list.length === 1) {
    return (
      <Link className="authors authors-link" href={`/authors/${list[0].slug}`}>
        {inner}
      </Link>
    );
  }
  return <div className="authors">{inner}</div>;
};
