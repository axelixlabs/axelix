import { createSlice } from "@reduxjs/toolkit";

import { type ICachesSliceState, IClearOperationType } from "models";
import { clearAllCachesThunk, clearCacheThunk, getCachesThunk } from "store/thunks";

const initialState: ICachesSliceState = {
    cacheManagers: [],
    loading: false,
    success: false,
    clearOperationLoading: null,
    error: "",
};

export const CachesSlice = createSlice({
    name: "cachesSlice",
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder.addCase(getCachesThunk.pending, (state) => {
            state.success = false;
            state.loading = true;
        });
        builder.addCase(getCachesThunk.fulfilled, (state, { payload }) => {
            state.loading = false;
            state.cacheManagers = payload.cacheManagers;
        });
        // todo fix any type after receiving real data from backend
        builder.addCase(getCachesThunk.rejected, (state, { payload }: any) => {
            const { status } = payload;

            state.loading = false;
            // todo change the logic in the future if needed, and also add a translation
            if (status >= 400 && status < 500) {
                state.error = "Неизвестная ошибка";
            } else {
                state.error = "Произошла внутренняя ошибка сервиса";
            }
        });

        builder.addCase(clearAllCachesThunk.pending, (state) => {
            state.success = false;
            state.clearOperationLoading = IClearOperationType.CLEAR_ALL_CACHES;
        });
        builder.addCase(clearAllCachesThunk.fulfilled, (state) => {
            state.success = true;
            state.clearOperationLoading = null;
        });
        // todo fix any type after receiving real data from backend
        builder.addCase(clearAllCachesThunk.rejected, (state, { payload }: any) => {
            const { status } = payload;
            state.success = false;
            state.clearOperationLoading = null;

            // todo change the logic in the future if needed, and also add a translation
            if (status >= 400 && status < 500) {
                state.error = "Неизвестная ошибка";
            } else {
                state.error = "Произошла внутренняя ошибка сервиса";
            }
        });

        builder.addCase(clearCacheThunk.pending, (state) => {
            state.success = false;
            state.clearOperationLoading = IClearOperationType.CLEAR_SINGLE_CACHE;
        });
        builder.addCase(clearCacheThunk.fulfilled, (state) => {
            state.clearOperationLoading = null;
            state.success = true;
        });
        builder.addCase(clearCacheThunk.rejected, (state, { payload }: any) => {
            const { status } = payload;
            state.success = false;
            state.clearOperationLoading = null;
            // todo change the logic in the future if needed, and also add a translation
            if (status >= 400 && status < 500) {
                state.error = "Неизвестная ошибка";
            } else {
                state.error = "Произошла внутренняя ошибка сервиса";
            }
        });
    },
});

export default CachesSlice;
