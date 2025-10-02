import {createAsyncThunk, createSlice, type PayloadAction,} from "@reduxjs/toolkit";

import type {IEnvironmentData, IEnvironmentSliceState} from "models";
import {getEnvironmentsData} from "services/environment";

const initialState: IEnvironmentSliceState = {
    loading: false,
    error: "",
    activeProfiles: [],
    defaultProfiles: [],
    propertySources: [],
    environmentSearchText: "",
    filteredPropertySources: [],
};

export const getEnvironmentsThunk = createAsyncThunk<IEnvironmentData, string, { rejectValue: any }>(
    "getEnvironmentsThunk",
    async (id: string, {rejectWithValue}) => {
        try {
            const response = await getEnvironmentsData(id);

            return response.data;

            // todo replace any with real type in future
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    });

export const EnvironmentSlice = createSlice({
    name: "environmentSlice",
    initialState,
    reducers: {
        filterProperties: (state, action: PayloadAction<string>) => {
            const searchText = action.payload.toLowerCase().trim();
            state.environmentSearchText = searchText;

            state.filteredPropertySources = state.propertySources.filter(
                ({name, properties}) => {
                    const filterByPropertySourcesName = name
                        .toLowerCase()
                        .includes(searchText);
                    const filterByPropertiesName = properties.some(({key}) =>
                        key.toLowerCase().includes(searchText)
                    );
                    return filterByPropertySourcesName || filterByPropertiesName;
                }
            );
        },
    },
    extraReducers: (builder) => {
        builder.addCase(getEnvironmentsThunk.pending, (state) => {
            state.loading = true;
        });
        builder.addCase(getEnvironmentsThunk.fulfilled, (state, {payload}) => {
            state.loading = false;
            state.activeProfiles = payload.activeProfiles;
            state.defaultProfiles = payload.defaultProfiles;
            state.propertySources = payload.propertySources;
        });
        builder.addCase(getEnvironmentsThunk.rejected, (state, {payload}) => {
            const {status} = payload;

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

export const {filterProperties} = EnvironmentSlice.actions;

export default EnvironmentSlice;
