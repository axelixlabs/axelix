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
import { createSlice } from "@reduxjs/toolkit";

import { StatelessRequest } from "models";
import { updatePropertyThunk } from "store/thunks";

const initialState: StatelessRequest = StatelessRequest.inactive();

export const UpdatePropertySlice = createSlice({
    name: "updatePropertySlice",
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder.addCase(updatePropertyThunk.pending, () => {
            return StatelessRequest.loading();
        });
        builder.addCase(updatePropertyThunk.fulfilled, () => {
            return StatelessRequest.success();
        });
        builder.addCase(updatePropertyThunk.rejected, (_, { payload }) => {
            const { code } = payload;
            return StatelessRequest.error(code);
        });
    },
});
