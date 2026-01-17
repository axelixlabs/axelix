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
import { createAsyncThunk } from "@reduxjs/toolkit";

import { extractErrorCode } from "helpers";
import type { IUpdatePropertyRequestData } from "models";
import { updateProperty } from "services";

// todo replace any with real type in future
export const updatePropertyThunk = createAsyncThunk<void, IUpdatePropertyRequestData, { rejectValue: any }>(
    "updatePropertyThunk",
    async (data, { rejectWithValue }) => {
        try {
            await updateProperty(data);

            // todo replace any with real type in future
        } catch (error: any) {
            return rejectWithValue({
                code: extractErrorCode(error?.response?.data),
            });
        }
    },
);
