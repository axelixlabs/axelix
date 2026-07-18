/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import {
    EInstallConfigurationVariant,
    ESpringBootVariant,
    INavLink,
    TInstallSpringBootArtifact,
    TInstallStepNames,
} from "@/models";
import { IInstallConfigurationOptions, IInstallSpringBootOptions } from "@/models";

export const NAV_LINKS: INavLink[] = [
    { href: "#reference-app", label: "Why Axelix?" },
    { href: "#capabilities", label: "Debugging" },
    { href: "#install", label: "Install" },
    { href: "#enterprise", label: "Enterprise" },
    { href: "#faq", label: "FAQ" },
];

export const DOCS_URL = "https://axelix.io/docs/product/introduction";
export const GITHUB_URL = "https://github.com/axelixlabs/axelix";
export const DEMO_APP_URL = "https://github.com/axelixlabs/order-service-demo";

export const installSpringBootArtifact: TInstallSpringBootArtifact = {
    [ESpringBootVariant.SPRING_BOOT_2]: "axelix-spring-boot-2-starter",
    [ESpringBootVariant.SPRING_BOOT_3]: "axelix-spring-boot-3-starter",
    [ESpringBootVariant.SPRING_BOOT_4]: "axelix-spring-boot-4-starter",
};

export const installStepNames: TInstallStepNames = {
    1: "Run Axelix",
    2: "Add Starter",
    3: "Add Plugin",
    4: "Configure",
};

export const installSpringBootOptions: IInstallSpringBootOptions[] = [
    {
        key: ESpringBootVariant.SPRING_BOOT_2,
        label: "Spring Boot 2",
    },
    {
        key: ESpringBootVariant.SPRING_BOOT_3,
        label: "Spring Boot 3",
    },
    {
        key: ESpringBootVariant.SPRING_BOOT_4,
        label: "Spring Boot 4",
    },
];

export const installConfigurationOptions: IInstallConfigurationOptions[] = [
    {
        key: EInstallConfigurationVariant.YAML,
        label: "yaml",
    },
    {
        key: EInstallConfigurationVariant.PROPERTIES,
        label: "properties",
    },
];
