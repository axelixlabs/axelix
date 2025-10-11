import { createAsyncThunk, createSlice, type PayloadAction, type WritableDraft, } from "@reduxjs/toolkit";

import type { IEnvironmentData, IEnvironmentPropertySource, IEnvironmentSliceState, IUpdatePropertyData } from "models";
import { getEnvironmentData, updateProperty } from "services";

const initialState: IEnvironmentSliceState = {
    loading: false,
    error: "",
    activeProfiles: [],
    defaultProfiles: [],
    propertySources: [],
    environmentSearchText: "",
    filteredPropertySources: [],
};

export const getEnvironmentThunk = createAsyncThunk<IEnvironmentData, string, { rejectValue: any }>(
    "getEnvironmentThunk",
    async (id: string, { rejectWithValue }) => {
        try {
            const response = await getEnvironmentData(id);

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

            state.filteredPropertySources = state.propertySources.filter(({ name, properties }) => {
                const filterByPropertySourcesName = name
                    .toLowerCase()
                    .includes(searchText);
                const filterByPropertiesName = properties.some(({ key }) => (
                    key.toLowerCase().includes(searchText)
                ));
                return filterByPropertySourcesName || filterByPropertiesName;
            }
            );
        }
    },
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

export const { filterProperties } = EnvironmentSlice.actions;

export default EnvironmentSlice;
