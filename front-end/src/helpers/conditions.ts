import type { ConditionBeanCollection } from "models";

export const filterMatches = (conditions: ConditionBeanCollection, search: string): ConditionBeanCollection => {
    const formattedSearch = search.toLowerCase().trim();

    return conditions.filter(({ target }) => {
        const lowerTarget = target.toLowerCase();

        if (lowerTarget.includes(formattedSearch)) {
            return true;
        }
    });
};
