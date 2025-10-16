import type { MenuItem } from "models";

export const findOpenKeys = (items: MenuItem[], path: string): string[] => {
    const parent = items.find(
        item => item && 'children' in item && item.children?.some(child => child?.key === path)
    );
    return parent ? [parent.key as string] : [];
};