import type { ConditionBeanCollection } from "models";

export const filterMatches = (conditions: ConditionBeanCollection, search: string): ConditionBeanCollection => {
    const formattedSearch = search.toLowerCase().trim();

    return conditions.filter(({ className, methodName }) => {
        if (className.toLowerCase().includes(formattedSearch)) {
            return true;
        }

        if (methodName?.toLowerCase().includes(formattedSearch)) {
            return true;
        }
    });
};
