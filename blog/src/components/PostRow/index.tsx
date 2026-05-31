import Link from "next/link";
import { Authors } from "../Authors";
import { DateMeta } from "../DateMeta";
import { TagRow } from "../TagRow";
import type { BlogCardItem } from "@/lib/source";
import styles from "./styles.module.css";

interface IProps {
  item: BlogCardItem;
}

export const PostRow = ({ item }: IProps) => {
  return (
    <Link className={styles.PostRow} href={item.href}>
      <div className={styles.Rbody}>
        <TagRow tags={item.tags} />
        <DateMeta date={item.date} readingMinutes={item.readingMinutes} />
        <h3>{item.title}</h3>
        {item.description && <p className={styles.Exc}>{item.description}</p>}
        <Authors authors={item.authors} />
      </div>
      {item.coverSrc ? (
        <div className={styles.Rcover}>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={item.coverSrc} alt="" />
        </div>
      ) : (
        <div className={`${styles.Rcover} ${styles.CoverPh}`} />
      )}
    </Link>
  );
};
