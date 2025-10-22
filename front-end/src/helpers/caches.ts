import type { ICachesManager } from "models";

export const filterCacheManagers = (cacheManagers: ICachesManager[], search: string): ICachesManager[] => {
    const formattedSearch = search.toLowerCase().trim();

    return cacheManagers.filter(({ name, caches }) => {
        const lowerName = name.toLowerCase();
        if (lowerName.includes(formattedSearch)) {
            return true;
        }

        return caches.some(({ name: cacheName }) => cacheName.toLowerCase().includes(formattedSearch));
    });
};
