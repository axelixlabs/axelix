interface IProps {
  minutes: number;
  className?: string;
}

export const ReadingTime = ({ minutes, className }: IProps) => {
  return <span className={className}>{minutes} min read</span>;
};
