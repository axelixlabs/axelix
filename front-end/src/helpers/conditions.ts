import type {INegativeMatches, IPositiveMatches} from "models";

export const filterMatches = (conditions:  INegativeMatches[] | IPositiveMatches[], search: string): INegativeMatches[] | IPositiveMatches[] => {
  const formattedSearch = search.toLowerCase().trim();

  return conditions.filter(({target}) => {
    const lowerTarget = target.toLowerCase();

    if (lowerTarget.includes(formattedSearch)) {
      return true
    }
  }) as INegativeMatches[] | IPositiveMatches[];
};