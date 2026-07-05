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
import { apiFetch } from "api";

import type { IDashboardPersistenceResponse } from "../models";

export const getDashboardData = () => {
    return apiFetch.get("dashboard");
};

export const getDashboardJavaData = () => {
    return apiFetch.get("/dashboard/java");
};

export const getDashboardPersistence = () => {
    return {
        nPlusOne: [
            { appName: "payments-service", size: 10 },
            { appName: "orders-service", size: 12 },
            { appName: "notification-service", size: 3 },
            { appName: "product-catalog-service", size: 7 },
            { appName: "api-gateway-service", size: 1 },
            { appName: "reporting-service", size: 11 },
            { appName: "dashboard-aggregation-service", size: 18 },
            { appName: "invoicing-service", size: 8 },
            { appName: "delivery-service", size: 2 },
            { appName: "audit-service", size: 2 },
        ],
        inMemoryPagination: [
            { appName: "payments-service", size: 7 },
            { appName: "orders-service", size: 8 },
            { appName: "notification-service", size: 3 },
            { appName: "product-catalog-service", size: 11 },
            { appName: "api-gateway-service", size: 1 },
            { appName: "reporting-service", size: 4 },
            { appName: "dashboard-aggregation-service", size: 3 },
            { appName: "invoicing-service", size: 8 },
            { appName: "delivery-service", size: 5 },
            { appName: "audit-service", size: 2 },
        ],
    } as IDashboardPersistenceResponse;
};

export const getDashboardSpringFramework = () => {
    return apiFetch.get("/dashboard/spring-framework");
};
