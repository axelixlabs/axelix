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
