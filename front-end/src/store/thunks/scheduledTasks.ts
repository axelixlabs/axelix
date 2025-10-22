import { createAsyncThunk } from "@reduxjs/toolkit";

import type { ScheduledTasksResponse } from "models";
import { getScheduledTasksData, updateScheduledTasksStatus } from "services";
import type { IUpdateScheduledTasksBody } from "models";

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


// todo add types in future for this thunk
export const updateScheduledTasksStatusThunk = createAsyncThunk(
    "updateScheduledTasksStatusThunk",
    async (responseBody: IUpdateScheduledTasksBody, { rejectWithValue }) => {
        try {
            await updateScheduledTasksStatus(responseBody);

            return ({
                target: responseBody.targetScheduledTask
            })

            // todo replace any with real type in future
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    });