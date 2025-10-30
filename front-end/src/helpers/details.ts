import { EDynamicIconsKeys, EDynamicIconsProperties } from "models";
import { detailsIcons } from "utils";

export const resolveIconFromContent = (title: string, content: string[][]): string | undefined => {
    const getValue = (key: string) => {
        return content.find(([name]) => name === key)?.[1];
    };

    switch (title) {
        case EDynamicIconsKeys.OS: {
            const osName = getValue("name")?.toLowerCase();
            // @ts-expect-error Fix in futurre
            if (osName && detailsIcons[osName]) {
                return osName;
            }

            return EDynamicIconsProperties.LINUX;
        }
        case EDynamicIconsKeys.RUNTIME: {
            return getValue("kotlinVersion") ? EDynamicIconsProperties.KOTLIN : EDynamicIconsProperties.JAVA;
        }
    }
};
