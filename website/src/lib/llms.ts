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
import { cards } from "@/components/Capabilities/CapabilitiesCompactCards";
import { featuredCards } from "@/components/Capabilities/CapabilitiesFeaturedCards";
import { EXTENSIONS } from "@/components/Enterprise/EnterpriseExtensionList";
import { FAQ_ITEMS } from "@/components/FAQ/FAQList/items";
import { LAYERS } from "@/components/HowExactly/layers";
import { INSTALLATION_METHODS } from "@/components/Install/Installer/installationMethods";
import { metrics } from "@/components/ReferenceApp/ResultMetrics";
import { DESCRIPTION, HEADLINE, INTRO } from "@/content/overview";
import { EInstallMethod } from "@/models";
import { BLOG_URL, DEMO_APP_URL, DOCS_URL, GITHUB_URL, SITE_URL } from "@/utils";

import { type ReactElement, type ReactNode, isValidElement } from "react";

/** Flatten the rich text already used by the UI without importing browser-only components. */
function markdownText(node: ReactNode): string {
    if (typeof node === "string" || typeof node === "number") return String(node);
    if (Array.isArray(node)) return node.map(markdownText).join("");
    if (!isValidElement(node)) return "";

    return markdownElement(node);
}

function markdownElement(node: ReactElement): string {
    const content = markdownText((node.props as { children?: ReactNode }).children);
    switch (node.type) {
        case "br":
            return "\n";
        case "strong":
        case "b":
            return `**${content}**`;
        case "em":
            return `*${content}*`;
        case "code":
            return `\`${content}\``;
        case "a": {
            const href = (node.props as { href?: string }).href;
            return href ? `[${content}](${href})` : content;
        }
        default:
            return content;
    }
}

export function buildLlmsIndex(): string {
    return `# Axelix

> ${DESCRIPTION}

## Website

- [Axelix homepage](${SITE_URL}/index.md): ${INTRO}
- [Full website content](${SITE_URL}/llms-full.txt): Product overview, capabilities, installation, Enterprise and FAQ.

## Documentation and blog

- [Documentation](${SITE_URL}/docs/llms.txt): Installation, configuration and feature reference.
- [Blog](${BLOG_URL}/llms.txt): Articles and engineering insights.
- [Source code](${GITHUB_URL}): Axelix open-source repository.
`;
}

const installMethods = [
    ["Docker", EInstallMethod.DOCKER],
    ["Docker Compose", EInstallMethod.COMPOSE],
    ["Kubernetes", EInstallMethod.K8S],
    ["Bare Metal / VMs", EInstallMethod.BARE],
] as const;

export function buildHomepageMarkdown(): string {
    const capabilities = [...featuredCards, ...cards]
        .map(({ title, description, href }) => `- [**${title}**](${href}): ${markdownText(description)}`)
        .join("\n");
    const results = metrics
        .map(({ label, value, unit, was, now }) => `- **${label}**: ${value} ${unit} (${was.amount} → ${now.amount})`)
        .join("\n");
    const problems = LAYERS.map(
        ({ title, subtitle, items }) =>
            `### ${title}\n\n${subtitle}\n\n${items
                .map(({ content, description }) => `- **${content}**: ${markdownText(description)}`)
                .join("\n")}`,
    ).join("\n\n");
    const installation = installMethods
        .map(([name, method]) => {
            const { description, href } = INSTALLATION_METHODS[method];
            return `- [**${name}**](${href}): ${description}`;
        })
        .join("\n");
    const extensions = EXTENSIONS.map(({ title, description }) => `- **${title}**: ${description}`).join("\n");
    const faq = FAQ_ITEMS.map(
        ({ question, answer }) =>
            `### ${question}\n\n${markdownText(answer)
                .replace(/[ \t]+\n/g, "\n")
                .replace(/\n[ \t]+/g, "\n")
                .replace(/\n{3,}/g, "\n\n")
                .trim()}`,
    ).join("\n\n");

    return `# ${HEADLINE} (${SITE_URL}/)

${INTRO}

## Why Axelix?

Axelix provides insights into running Java applications and helps engineers apply them without replacing their framework. Its Spring Boot starter gathers application information and Axelix Master makes it available to humans through a web console and to AI agents through an embedded MCP server. One role model governs both.

The [reference application](${DEMO_APP_URL}) uses Java 25, Spring Boot 3.5, Spring Data JPA, Hibernate, PostgreSQL and Kafka. Applying Axelix's suggestions produced these example results:

${results}

## How exactly?

Axelix surfaces problems across the JVM, code and application configuration layers. These simplified examples reflect real-world conditions:

${problems}

## Debugging capabilities

${capabilities}

## Install

Run Axelix Master, add the starter for Spring Boot 2, 3 or 4, and add the Maven or Gradle build plugin. No JVM agent or special boot order is required. Choose a deployment method:

${installation}

For setup steps, see the [getting started guide](${SITE_URL}/docs/start/getting-started).

## Enterprise · early access

Axelix OSS remains open source. Enterprise adds governance, security and support on top of the same console:

${extensions}

For enterprise pilots, contact [enterprise@axelix.io](mailto:enterprise@axelix.io).

## FAQ

${faq}

## More information

- [Documentation](${DOCS_URL})
- [Blog](${BLOG_URL}/llms.txt)
- [GitHub](${GITHUB_URL})
`;
}

export function buildLlmsFull(): string {
    return `# Axelix Website

Website: ${SITE_URL}/
Documentation: ${SITE_URL}/docs/llms.txt
Blog: ${BLOG_URL}/llms.txt

${buildHomepageMarkdown()}`;
}
