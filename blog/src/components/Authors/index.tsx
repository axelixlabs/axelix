import { getAuthors } from "@/lib/authors";
import { Avatar } from "../Avatar";

interface IProps {
  authors: string[];
}

/** One or many authors — stacked avatars + a summarized name label. */
export const Authors = ({ authors }: IProps) => {
  const list = getAuthors(authors);
  const names = list.map((a) => a.name);
  const label =
    names.length === 1
      ? names[0]
      : names.length === 2
        ? `${names[0]}, ${names[1]}`
        : `${names[0]} +${names.length - 1}`;
  return (
    <div className="authors">
      <span className="avatars">
        {list.slice(0, 3).map((a) => (
          <Avatar key={a.slug} authorRef={a.name} />
        ))}
      </span>
      <span className="who">{label}</span>
    </div>
  );
};
