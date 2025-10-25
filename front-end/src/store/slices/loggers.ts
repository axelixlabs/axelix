import { createSlice } from "@reduxjs/toolkit";

import type { ILoggersSliceState } from "models";
import { setLoggerLevelThunk } from "store/thunks";

const initialState: ILoggersSliceState = {
    loading: false,
    updateLoggerSuccess: false,
    levels: [],
    loggers: [],
    error: "",
};

export const LoggersSlice = createSlice({
    name: "loggersSlice",
    initialState,
    reducers: {
        resetUpdateLoggerSuccess: (state) => {
            state.updateLoggerSuccess = false;
        },
    },
    extraReducers: (builder) => {
        builder.addCase(setLoggerLevelThunk.pending, (state) => {
            state.loading = true;
            state.updateLoggerSuccess = false;
        });
        builder.addCase(setLoggerLevelThunk.fulfilled, (state) => {
            state.loading = false;
            state.updateLoggerSuccess = true;
        });
        // todo fix this in future
        builder.addCase(setLoggerLevelThunk.rejected, (state, { payload }: any) => {
            const { status } = payload;
            state.updateLoggerSuccess = false;
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

export const { resetUpdateLoggerSuccess } = LoggersSlice.actions;
