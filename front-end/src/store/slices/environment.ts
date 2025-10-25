import { createSlice } from "@reduxjs/toolkit";

import type { IEnvironmentSliceState } from "models";
import { getEnvironmentThunk } from "store/thunks";

const initialState: IEnvironmentSliceState = {
    loading: false,
    error: "",
    activeProfiles: [],
    defaultProfiles: [],
    propertySources: [],
};

export const EnvironmentSlice = createSlice({
    name: "environmentSlice",
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder.addCase(getEnvironmentThunk.pending, (state) => {
            state.loading = true;
        });
        builder.addCase(getEnvironmentThunk.fulfilled, (state, { payload }) => {
            state.loading = false;
            state.activeProfiles = payload.activeProfiles;
            state.defaultProfiles = payload.defaultProfiles;
            state.propertySources = payload.propertySources;
        });
        builder.addCase(getEnvironmentThunk.rejected, (state, { payload }) => {
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
