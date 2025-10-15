import { createSlice } from "@reduxjs/toolkit";

import type { IScheduledTaskItem, IScheduledTasksSliceState } from "models";
import { getScheduledTasksThunk, updateScheduledTasksStatusThunk } from "store/thunks";

const initialState: IScheduledTasksSliceState = {
    loading: false,
    updateScheduleTasksStatusLoading: false,
    error: "",
    scheduledTasksTypes: [],
};

export const ScheduledTasksSlice = createSlice({
    name: "scheduledTasksSlice",
    initialState,
    reducers: {},
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

        builder.addCase(updateScheduledTasksStatusThunk.pending, (state) => {
            state.updateScheduleTasksStatusLoading = true;
        });
        builder.addCase(updateScheduledTasksStatusThunk.fulfilled, (state, { payload }) => {
            state.updateScheduleTasksStatusLoading = false;
            const { target } = payload;

            state.scheduledTasksTypes.forEach(taskTypeItem => {
                taskTypeItem.tasks.forEach(task => {
                    if (task.runnable.target === target) {
                        task.enabled = !task.enabled;
                    }
                });
            });
        });
        builder.addCase(updateScheduledTasksStatusThunk.rejected, (state, { payload }) => {
            const { status } = payload;

            state.updateScheduleTasksStatusLoading = false;
            if (status >= 400 && status < 500) {
                // todo translate this in future
                state.error = "Неизвестная ошибка";
            } else {
                state.error = "Произошла внутренняя ошибка сервиса";
            }
        });
    },
});

export default ScheduledTasksSlice;
