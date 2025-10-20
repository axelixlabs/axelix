import { createAsyncThunk } from "@reduxjs/toolkit";

import type { IConfigPropsBeanData } from "models";
import { getConfigPropsData } from "services";

export const getConfigPropsThunk = createAsyncThunk<IConfigPropsBeanData, string, { rejectValue: any }>(
    "getConfigPropsThunk",
    async (id, { rejectWithValue }) => {
        try {
            const response = await getConfigPropsData(id);

            return response.data;
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    });