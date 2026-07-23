import { formatDate } from "@/lib/format"
import { Authors } from "../Authors"
import { ReadingTime } from "../ReadingTime"
import { PlainTag } from "../PlainTag"
import { BlogPage } from "@/lib/source"
import { computeReadingTime } from "@/lib/reading-time"

interface IProps {
    data: BlogPage["data"]
}

export const ArticleHeader = async ({ data }: IProps) => {
    const { title, description, authors, tags, date } = data

    const raw = await data.getText("raw");
    const readingMinutes = computeReadingTime(raw);

    return (
        <header className="art-hero">
            <h1>{title}</h1>
            {description && <p className="standfirst">{description}</p>}
            <div className="art-meta">
                <Authors authors={authors} />
                <span className="sep" />
                <span className="m">{formatDate(date)}</span>
                <span className="sep" />
                <ReadingTime minutes={readingMinutes} className="m" />
            </div>
            <div className="rtags">
                {(tags ?? []).map((tag) => (
                    <PlainTag label={tag} href={`/?tag=${encodeURIComponent(tag)}`} key={tag} />
                ))}
            </div>
        </header>
    )
}