import { createAsyncThunk } from "@reduxjs/toolkit";

import { getScheduledTasksData } from "services";
import type { ScheduledTasksResponse } from "models";

export const getScheduledTasksThunk = createAsyncThunk<ScheduledTasksResponse, string, { rejectValue: any }>(
    "getScheduledTasksThunk",
    async (instanceId, { rejectWithValue }) => {
        try {
            const response = await getScheduledTasksData(instanceId);

            return response.data;

            // todo replace any with real type in future
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    });