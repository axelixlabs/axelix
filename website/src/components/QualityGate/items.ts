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
export type TSeverity = "blocker" | "warning" | "discontinued" | "maintenance" | "dormant";
export type TLanguage = "java" | "yaml" | "xml";

export interface IFinding {
    rule: string;
    severity: TSeverity;
    body: string;
}

export interface IQualityExample {
    tab: string;
    file: string;
    path: string;
    language: TLanguage;
    kicker: string;
    title: string;
    intro: string;
    code: string[];
    // 1-based line number → 1-based finding number.
    flags: Record<number, number>;
    findings: IFinding[];
}

export const SEVERITY_COLORS: Record<TSeverity, { mark: string; background: string; text: string }> = {
    blocker: { mark: "#f28b7d", background: "rgb(242 139 125 / 18%)", text: "#9a3324" },
    warning: { mark: "#e6c26e", background: "rgb(230 194 110 / 25%)", text: "#7a5a10" },
    discontinued: { mark: "#f28b7d", background: "rgb(242 139 125 / 18%)", text: "#9a3324" },
    maintenance: { mark: "#e6c26e", background: "rgb(230 194 110 / 25%)", text: "#7a5a10" },
    dormant: { mark: "#a9b8c9", background: "rgb(120 140 165 / 18%)", text: "#3d4f63" },
};

export const QUALITY_EXAMPLES: IQualityExample[] = [
    {
        tab: "Code",
        file: "Order.java",
        path: "src/main/java/com/acme/orders/Order.java",
        language: "java",
        kicker: "Code",
        title: "Mappings that look fine and break under load.",
        intro: "JPA and Lombok shortcuts pass code review and show up in production as extra queries, lost rows and slow writes.",
        code: [
            "@Entity",
            "@Data",
            '@Table(name = "orders")',
            "public class Order {",
            "",
            "    @Id",
            "    @GeneratedValue",
            "    private Long id;",
            "",
            "    @OneToMany(cascade = CascadeType.ALL)",
            "    private List<OrderLine> lines;",
            "",
            "    @ManyToMany",
            "    private List<Tag> tags;",
            "}",
        ],
        flags: { 2: 1, 10: 2, 14: 3 },
        findings: [
            {
                rule: "@Data on a JPA entity",
                severity: "warning",
                body: "Generated equals, hashCode and toString touch lazy associations and trigger extra queries.",
            },
            {
                rule: "CascadeType.ALL on @OneToMany",
                severity: "blocker",
                body: "Includes REMOVE: deleting an order silently deletes every line. Cascade only PERSIST and MERGE.",
            },
            {
                rule: "List on @ManyToMany",
                severity: "blocker",
                body: "A List is a bag: removing one tag deletes all join rows and re-inserts the rest. Use a Set.",
            },
        ],
    },
    {
        tab: "Configuration",
        file: "application.yml",
        path: "src/main/resources/application.yml",
        language: "yaml",
        kicker: "Configuration",
        title: "Settings that work on a laptop.",
        intro: "Common properties that make development easier and cause trouble in production.",
        code: [
            "spring:",
            "  application:",
            "    name: orders-service",
            "  datasource:",
            "    url: jdbc:postgresql://db:5432/orders",
            "  jpa:",
            "    show-sql: true",
            "    properties:",
            "      hibernate:",
            "        enable_lazy_load_no_trans: true",
            "management:",
            "  endpoints:",
            "    web:",
            "      exposure:",
            '        include: "*"',
        ],
        flags: { 7: 1, 10: 2, 15: 3 },
        findings: [
            {
                rule: "show-sql: true",
                severity: "warning",
                body: "Prints every statement to stdout, bypassing your logging setup and flooding logs under load.",
            },
            {
                rule: "enable_lazy_load_no_trans",
                severity: "blocker",
                body: "Each lazy access outside a transaction opens a new session and connection. Hides N+1 queries and exhausts the pool.",
            },
            {
                rule: 'exposure.include: "*"',
                severity: "blocker",
                body: "Publishes heapdump, env and threaddump over HTTP. A heap dump holds credentials and user data.",
            },
        ],
    },
    {
        tab: "Dependencies",
        file: "pom.xml",
        path: "pom.xml",
        language: "xml",
        kicker: "Dependencies",
        title: "Libraries that stopped moving.",
        intro: "Axelix sorts what is on the classpath into three risks: discontinued, in maintenance mode, and dormant, with no release in a long time.",
        code: [
            "<dependencies>",
            "  <dependency>",
            "    <groupId>org.springframework.statemachine</groupId>",
            "    <artifactId>spring-statemachine-starter</artifactId>",
            "  </dependency>",
            "  <dependency>",
            "    <groupId>com.google.code.gson</groupId>",
            "    <artifactId>gson</artifactId>",
            "  </dependency>",
            "  <dependency>",
            "    <groupId>org.powermock</groupId>",
            "    <artifactId>powermock-module-junit4</artifactId>",
            "    <version>2.0.9</version>",
            "  </dependency>",
            "</dependencies>",
        ],
        flags: { 4: 1, 8: 2, 12: 3 },
        findings: [
            {
                rule: "Spring Statemachine",
                severity: "discontinued",
                body: "Officially discontinued and no longer maintained. No further fixes, including security fixes, will follow.",
            },
            {
                rule: "Gson",
                severity: "maintenance",
                body: "In maintenance mode, with no new features and only limited fixes. Moving off it is strongly advised.",
            },
            {
                rule: "PowerMock",
                severity: "dormant",
                body: "Not declared discontinued, but there has been no release since 2.0.9 in 2020.",
            },
        ],
    },
];
