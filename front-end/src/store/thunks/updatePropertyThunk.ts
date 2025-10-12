import { createAsyncThunk } from "@reduxjs/toolkit";

import type { IUpdateProperty } from "models";
import { updateProperty } from "services";

// todo replace any with real type in future
export const updatePropertyThunk = createAsyncThunk<void, IUpdateProperty, { rejectValue: any }>(
    "updatePropertyThunk",
    async ({ instanceId, updatePropertyData }, { rejectWithValue }) => {
        const { propertyName, newValue } = updatePropertyData

        try {
            await updateProperty(instanceId, {
                propertyName,
                newValue,
            });

            // todo replace any with real type in future
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    });