interface IProps {
  /** Pre-computed minutes (see `computeReadingTime` in `@/lib/reading-time`). */
  minutes: number;
  /** Optional class so each call site keeps its own styling. */
  className?: string;
}

/** Renders the "N min read" label. */
export const ReadingTime = ({ minutes, className }: IProps) => {
  return <span className={className}>{minutes} min read</span>;
};
