import { formatDate } from "@/lib/format";
import { ReadingTime } from "../ReadingTime";
import styles from "./styles.module.css";

interface IProps {
  date: string;
  readingMinutes: number;
}

export const DateMeta = ({ date, readingMinutes }: IProps) => {
  return (
    <div className={styles.DateMeta}>
      <span>{formatDate(date)}</span>
      <span className={styles.Dot} />
      <ReadingTime minutes={readingMinutes} className={styles.Rt} />
    </div>
  );
};
