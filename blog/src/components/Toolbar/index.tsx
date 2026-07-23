"use client";
import Link from "next/link";
import {useSearchContext} from "fumadocs-ui/contexts/search";
import {SHOW_ALL, colorForTag} from "@/lib/tags";
import {Chip} from "./Chip";
import styles from "./styles.module.css";
import {SearchBar} from "./SearchBar";
import {TagSelect} from "./TagSelect";
import {DEFAULT_CHIP_STYLE, VISIBLE_TAG_COUNT} from "@/utils";
import {MoreTags} from "./MoreTags";
import {chipColorStyle} from "@/helpers";

interface IProps {
    currentTag: string;
    tags: string[];
}

export const Toolbar = ({currentTag, tags}: IProps) => {
    const {setOpenSearch} = useSearchContext();

    const visibleTags = tags.slice(0, VISIBLE_TAG_COUNT);

    return (
        <div className={styles.MainWrapper}>
            <div className="wrap">
                <div className={styles.ToolbarRow}>
                    <div className={styles.ChipsWrapper}>
                        <Link href="/" style={DEFAULT_CHIP_STYLE}>
                            <Chip active={currentTag === SHOW_ALL}>
                                All
                            </Chip>
                        </Link>

                        {visibleTags.map((tag) => {
                            return (
                                <Link
                                    key={tag}
                                    href={`/?tag=${encodeURIComponent(tag)}`}
                                    style={chipColorStyle(colorForTag(tag))}
                                >
                                    <Chip active={currentTag === tag}>
                                        {tag}
                                    </Chip>
                                </Link>
                            )
                        })}

                        <MoreTags tags={tags} currentTag={currentTag} />
                    </div>

                    <TagSelect tags={tags} currentTag={currentTag}/>

                    <SearchBar onClick={() => setOpenSearch(true)}/>
                </div>
            </div>
        </div>
    );
};