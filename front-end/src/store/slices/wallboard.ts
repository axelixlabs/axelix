import { createSlice } from "@reduxjs/toolkit";

import type { IWallboardSliceState } from "models";
import { getWallboardDataThunk } from "store/thunks";

// import { getWallboardData } from "services";

const initialState: IWallboardSliceState = {
    loading: false,
    error: "",
    instances: [],
};

export const WallboardSlice = createSlice({
    name: "wallboardSlice",
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder.addCase(getWallboardDataThunk.pending, (state) => {
            state.loading = true;
        });
        builder.addCase(getWallboardDataThunk.fulfilled, (state, { payload }) => {
            state.loading = false;
            state.instances = payload.instances;
        });
        builder.addCase(getWallboardDataThunk.rejected, (state, { payload }: any) => {
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

export default WallboardSlice;
