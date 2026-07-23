import Link from "next/link";
import { Authors } from "../Authors";
import { DateMeta } from "../DateMeta";
import { TagRow } from "../TagRow";
import type { BlogCardItem } from "@/lib/source";
import styles from "./styles.module.css";
import Image from "next/image";

interface IProps {
  item: BlogCardItem;
}

export const PostRow = ({ item }: IProps) => {
  const { authors, coverSrc, date, description, href, readingMinutes, tags, title } = item

  return (
    <>
      <Link className={styles.MainWrapper} href={href}>
        <div className={styles.ContentWrapper}>
          <TagRow tags={tags} />
          <DateMeta date={date} readingMinutes={readingMinutes} />
          <h3 className={styles.Title}>{title}</h3>
          {description && <p className={styles.Description}>{description}</p>}
          
          <Authors authors={authors} />
        </div>

        {coverSrc ? (
          <div className={styles.CoverImageWrapper}>
            <Image
              src={coverSrc}
              alt={title}
              fill
              sizes="(max-width: 760px) 100vw, 50vw"
              priority
              className={styles.CoverImage}
            />
          </div>
        ) : (
          <div className={styles.CoverImagePlaceholder} />
        )}
      </Link>
    </>
  );
};
