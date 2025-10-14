import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

import type { IScheduledTaskItem, IScheduledTasksSliceState } from "models";
import { getScheduledTasksThunk } from "store/thunks";

const initialState: IScheduledTasksSliceState = {
    loading: false,
    error: "",
    scheduledTasksTypes: [],
    scheduledTasksSearchText: "",
    filteredScheduledTasksTypes: []
};

export const ScheduledTasksSlice = createSlice({
    name: "scheduledTasksSlice",
    initialState,
    reducers: {
        filterScheduledTasks: (state, action: PayloadAction<string>) => {
            const searchText = action.payload.toLowerCase().trim();
            state.scheduledTasksSearchText = searchText;

            state.filteredScheduledTasksTypes = state.scheduledTasksTypes.filter(({ tasks }) => {
                return tasks.some(({ runnable }) => (
                    runnable.target.toLowerCase().includes(searchText)
                ));
            });
        }
    },
    extraReducers: (builder) => {
        builder.addCase(getScheduledTasksThunk.pending, (state) => {
            state.loading = true;
        });
        builder.addCase(getScheduledTasksThunk.fulfilled, (state, { payload }) => {
            state.loading = false;

            state.scheduledTasksTypes = Object.entries(payload).map(([type, tasks]) => ({
                type: type as IScheduledTaskItem["type"],
                tasks: tasks
            }));
        });
        builder.addCase(getScheduledTasksThunk.rejected, (state, { payload }) => {
            const { status } = payload;

            state.loading = false;
            if (status >= 400 && status < 500) {
                // todo translate this in future
                state.error = "Неизвестная ошибка";
            } else {
                state.error = "Произошла внутренняя ошибка сервиса";
            }
        });
    },
});

export const { filterScheduledTasks } = ScheduledTasksSlice.actions

export default ScheduledTasksSlice;
