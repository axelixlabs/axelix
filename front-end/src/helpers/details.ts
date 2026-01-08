/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import FreeBSD from "assets/icons/freeBSD.svg?react";
import JavaIcon from "assets/icons/java.svg?react";
import KotlinIcon from "assets/icons/kotlin.svg?react";
import LinuxIcon from "assets/icons/linux.svg?react";
import WindowsIcon from "assets/icons/windows.svg?react";
import type { FC, SVGProps } from "react";

import { ECopyableField, type IDetailsRuntime } from "models";

/**
 * Resolve the Icon to be used on the details card for the given Operating System.
 *
 * @param osName operating system name
 */
export function resolveOsIcon(osName: string): FC<SVGProps<SVGSVGElement>> {
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
    return (Object.values(ECopyableField) as string[]).includes(field);
}

/**
 * Resolve the Icon to be used on the details card for the given runtime.
 *
 * @param runtime the runtime information
 */
export function resolveLangIcon(runtime: IDetailsRuntime): FC<SVGProps<SVGSVGElement>> {
    return runtime.kotlinVersion ? KotlinIcon : JavaIcon;
}

export const downloadFile = (file: Blob) => {
    const blob = new Blob([file], { type: "application/zip" });

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.click();
    window.URL.revokeObjectURL(url);
};
