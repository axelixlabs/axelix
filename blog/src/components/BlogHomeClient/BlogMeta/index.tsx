import { SHOW_ALL } from "@/lib/tags";
import styles from "./styles.module.css"
import { BlogCardItem } from "@/lib/source";

interface IProps {
    byTag: BlogCardItem[];
    currentTag: string;
}

export const BlogMeta = ({ byTag, currentTag }: IProps) => {
    if (currentTag === SHOW_ALL) {
        return null
    }

    return (
        <div className={styles.MainWrapper}>
            <b>{byTag.length}</b> {byTag.length === 1 ? "article" : "articles"} tagged{" "}
            <b>{currentTag}</b>
        </div>
    )
} 