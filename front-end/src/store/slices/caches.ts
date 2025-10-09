import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

import { clearAllCachesThunk, clearCacheThunk, getCachesThunk } from "store/thunks";
import type { ICachesSliceState } from "models";

const initialState: ICachesSliceState = {
    cacheManagersSearchText: "",
    filteredCacheManagers: [],
    cacheManagers: [],
    loading: false,
    success: false,
    clearLoading: "",
    error: ""
};

export const CachesSlice = createSlice({
    name: "cachesSlice",
    initialState,
    reducers: {
        filterCacheManagers: (state, action: PayloadAction<string>) => {
            const searchText = action.payload.toLowerCase().trim();
            state.cacheManagersSearchText = searchText;

            state.filteredCacheManagers = state.cacheManagers.filter((cacheManager) => {
                const filterByCacheManagerName = cacheManager.name
                    .toLowerCase()
                    .includes(searchText);

                const filterByAliases = cacheManager.caches.some(({ name }) =>
                    name.toLowerCase().includes(searchText)
                );

                return filterByCacheManagerName || filterByAliases;
            });
        },
    },
    extraReducers: (builder) => {
        builder.addCase(getCachesThunk.pending, (state) => {
            state.cacheManagersSearchText = ""
            state.clearLoading = ""
            state.success = false
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
            state.success = false
            state.clearLoading = "allCaches"
        });
        builder.addCase(clearAllCachesThunk.fulfilled, (state) => {
            state.success = true
            state.clearLoading = ""
        });
        // todo fix any type after receiving real data from backend
        builder.addCase(clearAllCachesThunk.rejected, (state, { payload }: any) => {
            const { status } = payload;
            state.success = false
            state.clearLoading = ""

            // todo change the logic in the future if needed, and also add a translation
            if (status >= 400 && status < 500) {
                state.error = "Неизвестная ошибка";
            } else {
                state.error = "Произошла внутренняя ошибка сервиса";
            }
        });

        builder.addCase(clearCacheThunk.pending, (state) => {
            state.success = false
            state.clearLoading = "singleCache"
        });
        builder.addCase(clearCacheThunk.fulfilled, (state) => {
            state.clearLoading = ""
            state.success = true
        });
        // todo fix any type after receiving real data from backend
        builder.addCase(clearCacheThunk.rejected, (state, { payload }: any) => {
            const { status } = payload;
            state.success = false
            state.clearLoading = ""
            // todo change the logic in the future if needed, and also add a translation
            if (status >= 400 && status < 500) {
                state.error = "Неизвестная ошибка";
            } else {
                state.error = "Произошла внутренняя ошибка сервиса";
            }
        });
    },
});

export const { filterCacheManagers } = CachesSlice.actions;

export default CachesSlice;
