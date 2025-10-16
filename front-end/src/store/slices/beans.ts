import { createSlice } from "@reduxjs/toolkit";

import type { IBeansSliceState } from "models";
import { getBeansThunk } from "store/thunks";

const initialState: IBeansSliceState = {
    loading: false,
    error: "",
    beans: []
};

export const BeansSlice = createSlice({
    name: "beansSlice",
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder.addCase(getBeansThunk.pending, (state) => {
            state.loading = true;
        });
        builder.addCase(getBeansThunk.fulfilled, (state, { payload }) => {
            state.loading = false;
            state.beans = payload.beans;
        });
        // todo fix any type after receiving real data from backend
        builder.addCase(getBeansThunk.rejected, (state, { payload }: any) => {
            const { status } = payload;

            state.loading = false;
            // todo change the logic in the future if needed, and also add a translation
            if (status >= 400 && status < 500) {
                state.error = "Неизвестная ошибка";
            } else {
                state.error = "Произошла внутренняя ошибка сервиса";
            }
        });
    },
});

export default BeansSlice;
