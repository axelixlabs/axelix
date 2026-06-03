import { PlainTag } from "../PlainTag";

interface IProps {
  tags: string[];
  max?: number;
}

/** Up to `max` tags, with a `+N` overflow chip. */
export const TagRow = ({ tags, max = 3 }: IProps) => {
  const shown = tags.slice(0, Math.max(0, max));
  const overflow = tags.length - shown.length;
  return (
    <div className="rtags">
      {shown.map((t) => (
        <PlainTag key={t} label={t} />
      ))}
      {overflow > 0 && <span className="tag tag-more">+{overflow}</span>}
    </div>
  );
};
