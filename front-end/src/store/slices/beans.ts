import {createAsyncThunk, createSlice, type PayloadAction} from "@reduxjs/toolkit";

import type {IBeansData, IBeansSliceState} from "models";
import {getBeansData} from "services/beans";

const initialState: IBeansSliceState = {
    loading: false,
    error: "",
    beans: [],
    beansSearchText: "",
    filteredBeans: [],
};

// todo remove any for rejectvalue in future
export const getBeansThunk = createAsyncThunk<IBeansData, string, { rejectValue: any }>(
    "getBeansThunk",
    async (id, {rejectWithValue}) => {
        try {
            const response = await getBeansData(id);

            return response.data;
            // todo fix any type after receiving real data from backend
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    });

export const BeansSlice = createSlice({
    name: "beansSlice",
    initialState,
    reducers: {
        filterBeans: (state, action: PayloadAction<string>) => {
            const searchText = action.payload.toLowerCase().trim();
            state.beansSearchText = searchText;

            state.filteredBeans = state.beans.filter((bean) => {
                const filterByBeanName = bean.beanName
                    .toLowerCase()
                    .includes(searchText);

                const filterByClassName = bean.className
                    .toLowerCase()
                    .includes(searchText);

                const filterByAliases = bean.aliases.some((alias) =>
                    alias.toLowerCase().includes(searchText)
                );

                return filterByBeanName || filterByClassName || filterByAliases;
            });
        },
    },
    extraReducers: (builder) => {
        builder.addCase(getBeansThunk.pending, (state) => {
            state.loading = true;
        });
        builder.addCase(getBeansThunk.fulfilled, (state, {payload}) => {
            state.loading = false;
            state.beans = payload.beans;
        });
        // todo fix any type after receiving real data from backend
        builder.addCase(getBeansThunk.rejected, (state, {payload}: any) => {
            const {status} = payload;

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

export const {filterBeans} = BeansSlice.actions;

export default BeansSlice;
