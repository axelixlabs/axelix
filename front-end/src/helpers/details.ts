import { detailsIcons } from "utils";

export const resolveIconFromContent = (title, content) => {
    const getValue = (key) => content.find(([name]) => name === key)?.[1];

    switch (title) {
        case "os": {
            const osName = getValue("name")?.toLowerCase();
            return detailsIcons[osName] ? osName : "linux";
        }
        case "runtime": {
            return getValue("kotlinVersion") ? "kotlin" : "java";
        }
        default:
            return undefined;
    }
};
