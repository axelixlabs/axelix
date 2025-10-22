import { createAsyncThunk } from "@reduxjs/toolkit";

import type { ISetLoggerLevelRequestData } from "models";
import { setLoggerLevel } from "services";

// todo fix any in future for rejectValue
export const setLoggerLevelThunk = createAsyncThunk<void, ISetLoggerLevelRequestData, { rejectValue: any }>(
    "setLoggerLevelThunk",
    async ({ instanceId, loggerName, loggingLevel }, { rejectWithValue }) => {
        try {
            await setLoggerLevel(instanceId, loggerName, loggingLevel);
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    },
);
