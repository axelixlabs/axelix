import { BlogCardItem } from "@/lib/source";
import Link from "next/link";
import { Authors, TagRow, DateMeta } from "@/components";

import styles from "./styles.module.css"
import Image from "next/image";

interface IProps {
    item: BlogCardItem;
}

export const FeaturedPost = ({ item }: IProps) => {
    const { href, coverSrc, tags, title, description, authors, date, readingMinutes } = item

    return (
        <>
            <Link className={styles.MainWrapper} href={href}>
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

                <div className={styles.ContentWrapper}>
                    <TagRow tags={tags} />
                    <DateMeta date={date} readingMinutes={readingMinutes} />
                    <h2 className={styles.PostTitle}>{title}</h2>
                    {description && <p className={styles.PostDescription}>{description}</p>}
                    <Authors authors={authors} />
                </div>
            </Link>
        </>
    );
};