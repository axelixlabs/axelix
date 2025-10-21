import { ConditionsAccordion } from "../ConditionsAccordion";

import styles from '../styles.module.css'
import type {INegativeMatches, IPositiveMatches} from "models";

interface IProps {
  /**
   * Match tab title
   */
  title: string;
  /**
   * Negative or positive matches
   */
  matches: INegativeMatches[] | IPositiveMatches[];
}

export const Matches = ({ title, matches }: IProps) => {
    return (
        <>
            <div className={`MediumTitle ${styles.ConditionsMainTitle}`}>
              {title}
            </div>

            {matches.map((match) => (
                <div key={match.target} className={styles.ConditionsListWrapper}>
                  <ConditionsAccordion match={match} />
                </div>
            ))}
        </>
    );
};
