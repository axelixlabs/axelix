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
    IHeaderMenu,
    TInstallSpringBootArtifact,
    TInstallStepNames,
} from "@/models";
import { IInstallConfigurationOptions, IInstallSpringBootOptions } from "@/models";

export const SITE_URL = "https://axelix.io";
export const DOCS_URL = "https://axelix.io/docs/product/introduction";
export const MOTIVATION_DOCS_URL = "https://axelix.io/docs/product/motivation";
export const BLOG_URL = "https://axelix.io/blog";
export const GITHUB_URL = "https://github.com/axelixlabs/axelix";
export const GITHUB_ISSUES_URL = "https://github.com/axelixlabs/axelix/issues";
export const CONTACT_EMAIL = "hello@axelix.io";
export const ENTERPRISE_EMAIL = "enterprise@axelix.io";

// Landing anchors are root-relative so they keep working from sub-pages.
export const ENTERPRISE_HREF = "/enterprise";
export const ABOUT_HREF = "/about";
export const COMMUNITY_HREF = "/community";
export const CONTACT_HREF = "/contact";
export const GET_STARTED_HREF = "/#install";

// Keep in sync with the "Install with an AI agent" prompt in the root README.md.
export const AI_INSTALL_PROMPT =
    "Install Axelix in my app: run `curl -s https://axelix.io/docs/start/SKILL.md` and use it as a guide";

export const HEADER_MENUS: IHeaderMenu[] = [
    {
        key: "product",
        label: "Product",
        columns: [
            {
                heading: "Product",
                links: [
                    { title: "Why", description: "Why now, and why we build Axelix.", href: "/#problem" },
                    {
                        title: "What it does",
                        description: "Catch code, config and library problems, and debug live services.",
                        href: "/#quality",
                    },
                    { title: "Install", description: "Add Axelix to a Spring Boot service.", href: GET_STARTED_HREF },
                    { title: "FAQ", description: "License, safety and platform support.", href: "/#faq" },
                ],
            },
        ],
    },
    {
        key: "resources",
        label: "Resources",
        columns: [
            {
                heading: "Learn",
                links: [
                    { title: "Docs", description: "Guides and reference.", href: DOCS_URL, external: true },
                    {
                        title: "Blog",
                        description: "Engineering articles from the team.",
                        href: BLOG_URL,
                        external: true,
                    },
                ],
            },
        ],
    },
    {
        key: "company",
        label: "Company",
        columns: [
            {
                heading: "Axelix",
                links: [
                    { title: "About", description: "Who builds Axelix and why.", href: ABOUT_HREF },
                    { title: "Community", description: "Contribute, events and talks.", href: COMMUNITY_HREF },
                    { title: "Contact", description: "Email, issues and security reports.", href: CONTACT_HREF },
                ],
            },
        ],
    },
];
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

export const getLinkTargetProps = (external?: boolean) =>
    external ? { target: "_blank", rel: "noopener noreferrer" } : {};
