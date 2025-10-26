import { createSlice } from "@reduxjs/toolkit";

import type { IConfigPropsSliceState } from "models";
import { getConfigPropsThunk } from "store/thunks";

const initialState: IConfigPropsSliceState = {
    loading: false,
    error: "",
    beans: [],
};

export const ConfigPropsSlice = createSlice({
    name: "configPropsSlice",
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder.addCase(getConfigPropsThunk.pending, (state) => {
            state.loading = true;
        });
        builder.addCase(getConfigPropsThunk.fulfilled, (state, { payload }) => {
            state.loading = false;
            state.beans = payload.beans;
        });
        builder.addCase(getConfigPropsThunk.rejected, (state, { payload }: any) => {
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
