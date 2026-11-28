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
import { EPlatformName, type ISpringPortfolioApplication } from "@/models";

export const PLATFORM_ORDER: EPlatformName[] = [EPlatformName.SPRING_BOOT, EPlatformName.SPRING_FRAMEWORK];

export const SPRING_PORTFOLIO_APPLICATIONS_PAGE_SIZE = 10;

export const SPRING_SLICE_MIN_APPLICATIONS = 3;

export const SPRING_PORTFOLIO_APPLICATIONS_MOCK: ISpringPortfolioApplication[] = [
    { name: "com.example:api-gateway", springBootLine: "4.0.x", springFrameworkLine: "7.0.x", ossSupported: true },
    { name: "com.example:billing-service", springBootLine: "3.5.x", springFrameworkLine: "6.2.x", ossSupported: false },
    { name: "com.example:order-service", springBootLine: "3.5.x", springFrameworkLine: "6.2.x", ossSupported: false },
    { name: "com.example:user-service", springBootLine: "4.0.x", springFrameworkLine: "7.0.x", ossSupported: true },
    { name: "com.example:payment-service", springBootLine: "3.4.x", springFrameworkLine: "6.2.x", ossSupported: false },
    {
        name: "com.example:inventory-service",
        springBootLine: "3.3.x",
        springFrameworkLine: "6.1.x",
        ossSupported: false,
    },
    {
        name: "com.example:notification-service",
        springBootLine: "3.5.x",
        springFrameworkLine: "6.2.x",
        ossSupported: false,
    },
    { name: "com.example:catalog-service", springBootLine: "3.2.x", springFrameworkLine: "6.1.x", ossSupported: false },
    { name: "com.example:auth-service", springBootLine: "4.0.x", springFrameworkLine: "7.0.x", ossSupported: true },
    {
        name: "com.example:reporting-service",
        springBootLine: "2.7.x",
        springFrameworkLine: "5.3.x",
        ossSupported: false,
    },
    { name: "com.example:legacy-crm", springBootLine: "2.7.x", springFrameworkLine: "5.3.x", ossSupported: false },
];
