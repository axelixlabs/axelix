/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
