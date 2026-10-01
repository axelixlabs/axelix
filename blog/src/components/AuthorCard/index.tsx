import Link from "next/link";
import { Avatar } from "../Avatar";
import { PlainTag } from "../PlainTag";
import { formatDate } from "@/lib/format";
import type { AuthorSummary } from "@/lib/source";
import styles from "./styles.module.css";

interface IProps {
  author: AuthorSummary;
}

/** One author on the authors index: avatar, name/role, bio, top topics, and a
 *  derived count + latest-article footer. */
export const AuthorCard = ({ author }: IProps) => {
  return (
    <Link className={styles.Card} href={author.href}>
      <div className={styles.Top}>
        <Avatar authorRef={author.name} size={64} />
        <div className={styles.Ident}>
          <div className={styles.Name}>{author.name}</div>
          {author.title && <div className={styles.Role}>{author.title}</div>}
        </div>
      </div>

      {author.bio && <p className={styles.Bio}>{author.bio}</p>}

      {author.topics.length > 0 && (
        <div className="rtags">
          {author.topics.slice(0, 3).map((topic) => (
            <PlainTag key={topic.name} label={topic.name} />
          ))}
        </div>
      )}

      <div className={styles.Foot}>
        <span>
          <b>{author.count}</b> {author.count === 1 ? "article" : "articles"}
        </span>
        <span>
          {author.latestDate ? `Latest ${formatDate(author.latestDate)}` : "No articles yet"}
        </span>
      </div>
    </Link>
  );
};
