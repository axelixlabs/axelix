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
import type { EInstanceStatus, EWallboardFilterKey, EWallboardFilterOperator, ISelectOptionData } from "models";

export interface IInstanceCard {
    instanceId: string;
    springBootVersion: string;
    javaVersion: string;
    status: EInstanceStatus;
    name: string;
    serviceVersion: string;
    commitShaShort: string;
    deployedFor: string;
}

export interface IServiceCardsResponseBody {
    instances: IInstanceCard[];
}

export interface IWallboardSingleOperandFilter {
    id: string;
    key: EWallboardFilterKey;
    operator: EWallboardFilterOperator;
    operand: string;
}

export interface IWallboardLocalFilterInitialState {
    key: EWallboardFilterKey | null;
    operator: EWallboardFilterOperator | null;
    operand: string | null;
}

export interface IWallboardFilterDefinition {
    key: string;
    operators: ISelectOptionData[];
    getSelectOptionsData: (instances: IInstanceCard[]) => ISelectOptionData[];
    match: (instance: IInstanceCard, filter: IWallboardSingleOperandFilter) => boolean;
}
