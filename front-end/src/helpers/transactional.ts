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
import type { ITransactionalEntryPoint } from "models";

export const filterTransactionalData = (
    transactionalData: ITransactionalEntryPoint[],
    search: string,
): ITransactionalEntryPoint[] => {
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

const formatTransactionalDuration = (value: number): string => {
    const seconds = value / 1000;
    const formatted = Number.isInteger(seconds) ? seconds.toString() : seconds.toFixed(1);

    return `${formatted}s`;
};

export const formatTransactionDuration = (value: number) => {
    return value < 1000 ? `${value} ms` : formatTransactionalDuration(value);
};
