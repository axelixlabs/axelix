import type { IInstanceCard } from "models";

export const filterInstances = (instances: IInstanceCard[], search: string): IInstanceCard[] => {
    const formattedSearch = search.toLowerCase().trim();

    return instances.filter(({ name }) => name.toLowerCase().includes(formattedSearch));
};
