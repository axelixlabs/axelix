import { createAsyncThunk, createSlice, type PayloadAction, } from "@reduxjs/toolkit";

import type { IEnvironmentData, IEnvironmentSliceState, IUpdateEnvConfigProperty } from "models";
import { getEnvironmentData, updateEnvConfigProperty } from "services";

const initialState: IEnvironmentSliceState = {
    loading: false,
    error: "",
    activeProfiles: [],
    defaultProfiles: [],
    propertySources: [],
    environmentSearchText: "",
    filteredPropertySources: [],
    success: false
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


// todo replace any with real type in future
export const updateEnvConfigPropertyThunk = createAsyncThunk<void, IUpdateEnvConfigProperty, { rejectValue: any }>(
    "updateEnvConfigPropertyThunk",
    async ({ instanceId, data }, { rejectWithValue }) => {
        try {
            await updateEnvConfigProperty(instanceId, {
                propertyName: data.propertyName,
                newValue: data.newValue,
            });
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
        },
    },
    extraReducers: (builder) => {
        builder.addCase(getEnvironmentThunk.pending, (state) => {
            state.loading = true;
            state.success = false
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

        builder.addCase(updateEnvConfigPropertyThunk.pending, (state) => {
            state.loading = true;
            state.success = false
        });
        builder.addCase(updateEnvConfigPropertyThunk.fulfilled, (state) => {
            state.loading = false;
            state.success = true
        });
        builder.addCase(updateEnvConfigPropertyThunk.rejected, (state, { payload }) => {
            const { status } = payload;
            state.success = false

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
