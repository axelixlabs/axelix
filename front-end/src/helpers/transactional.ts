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
import type { ITransactionalResponseData } from "models";

export const filterTransactionalData = (
    transactionalData: ITransactionalResponseData[],
    search: string,
): ITransactionalResponseData[] => {
    const formattedSearch = search.toLowerCase().trim();

    return transactionalData.filter(({ className, methodName }) => {
        const lowerClassName = className.toLowerCase();
        if (lowerClassName.includes(formattedSearch)) {
            return true;
        }

        const lowerMethodName = methodName.toLowerCase();
        if (lowerMethodName.includes(formattedSearch)) {
            return true;
        }
    });
};

export const formatExecutionTimestamp = (value: string): string => {
    // TODO: Consider the option of correct time display
    return new Date(Number(value) * 1000).toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
    });
};
