import { createAsyncThunk } from "@reduxjs/toolkit";

import type { ICachesData, IClearCachePayload } from "models";
import { clearAllCachesData, clearCacheData, getCachesData } from "services";

export const getCachesThunk = createAsyncThunk<ICachesData, string, { rejectValue: any }>(
    "getCachesThunk",
    async (instanceId, { rejectWithValue }) => {
        try {
            const response = await getCachesData(instanceId);

            return response.data;
            // todo fix any type after receiving real data from backend
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    },
);

export const clearAllCachesThunk = createAsyncThunk<void, string, { rejectValue: any }>(
    "clearAllCachesThunk",
    async (instanceId, { rejectWithValue }) => {
        try {
            await clearAllCachesData(instanceId);
            // todo fix any type after receiving real data from backend
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    },
);

export const clearCacheThunk = createAsyncThunk<void, IClearCachePayload, { rejectValue: any }>(
    "clearCacheThunk",
    async ({ instanceId, data }, { rejectWithValue }) => {
        try {
            await clearCacheData(instanceId, data);
            // todo fix any type after receiving real data from backend
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    },
);
