import { createAsyncThunk } from "@reduxjs/toolkit";

import type { IUpdateProperty } from "models";
import { updateProperty } from "services";
import { localeUpdateConfigPropsProperty, localeUpdateEnvProperty } from "store/slices";

// todo replace any with real type in future
export const updatePropertyThunk = createAsyncThunk<void, IUpdateProperty, { rejectValue: any }>(
    "updatePropertyThunk",
    async ({ instanceId, data }, { rejectWithValue, dispatch }) => {
        const { propertySourceName, propertyName, newValue } = data

        try {
            await updateProperty(instanceId, {
                propertyName,
                newValue,
            });

            const page = location.pathname.split('/').filter(Boolean).pop()
            // todo
            const localeUpdateType = page === 'environment' ? localeUpdateEnvProperty : localeUpdateConfigPropsProperty

            dispatch(localeUpdateType({
                propertySourceName,
                propertyName,
                newValue,
            }))

            // todo replace any with real type in future
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    });