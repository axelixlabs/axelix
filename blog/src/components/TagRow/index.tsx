import { PlainTag } from "../PlainTag";

interface IProps {
  tags: string[];
}

const MAX_VISIBLE_TAGS = 3

export const TagRow = ({ tags }: IProps) => {
  const visibleTags = tags.slice(0, Math.max(0, MAX_VISIBLE_TAGS));
  const overflowTags = tags.length - visibleTags.length;

  return (
    <div className="rtags">
      {visibleTags.map((tag) => (
        <PlainTag label={tag} key={tag} />
      ))}

      {overflowTags && <span className="tag tag-more">+{overflowTags}</span>}
    </div>
  );
};
