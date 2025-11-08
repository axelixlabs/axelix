import FreeBSD from "assets/icons/freeBSD.svg";

import { ECopyableField, type IDetailsRuntime } from "models";

import JavaIcon from "assets/icons/java.svg";
import KotlinIcon from "assets/icons/kotlin.svg";
import LinuxIcon from "assets/icons/linux.svg";
import WindowsIcon from "assets/icons/windows.svg";

/**
 * Resolve the Icon to be used on the details card for the given Operating System.
 *
 * @param osName operating system name
 */
export function resolveOsIcon(osName: string): string {
    switch (osName.trim().toLowerCase()) {
        case "windows":
            return WindowsIcon;
        case "freebsd":
            return FreeBSD;
        default:
            return LinuxIcon;
    }
}

export function isCopyableField(field: string): boolean {
    const isCopyable = (Object.values(ECopyableField) as string[]).includes(field);
    return isCopyable;
}

/**
 * Resolve the Icon to be used on the details card for the given runtime.
 *
 * @param runtime the runtime information
 */
export function resolveLangIcon(runtime: IDetailsRuntime): string {
    return runtime.kotlinVersion ? KotlinIcon : JavaIcon;
}
