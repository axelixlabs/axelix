import { createAsyncThunk } from "@reduxjs/toolkit";

import type { IEnvironmentData } from "models";
import { getEnvironmentData } from "services";

export const getEnvironmentThunk = createAsyncThunk<IEnvironmentData, string, { rejectValue: any }>(
    "getEnvironmentThunk",
    async (id: string, { rejectWithValue }) => {
        try {
            const response = await getEnvironmentData(id);

            return response.data;

            // todo replace any with real type in future
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    },
);
