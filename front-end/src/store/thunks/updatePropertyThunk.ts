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
