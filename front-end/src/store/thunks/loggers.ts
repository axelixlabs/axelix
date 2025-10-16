import { createAsyncThunk } from "@reduxjs/toolkit";

import type { ILoggerData, ISetLoggerLevelRequestData } from "models";
import { getLoggersData, setLoggerLevel } from "services";

// todo fix any in future for rejectValue
export const getLoggersThunk = createAsyncThunk<ILoggerData, string, { rejectValue: any }>(
    "getLoggersThunk",
    async (instanceId, { rejectWithValue }) => {
        try {
            const response = await getLoggersData(instanceId);

            return response.data;
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    }
);

// todo fix any in future for rejectValue
export const setLoggerLevelThunk = createAsyncThunk<void, ISetLoggerLevelRequestData, { rejectValue: any }>(
    "setLoggerLevelThunk",
    async ({ instanceId, loggerName, loggingLevel }, { dispatch, rejectWithValue }) => {
        try {
            await setLoggerLevel(instanceId, loggerName, loggingLevel)
            dispatch(getLoggersThunk(instanceId));
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    }
);