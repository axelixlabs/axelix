import { createAsyncThunk } from "@reduxjs/toolkit";

import type { IBeansData } from "models";
import { getBeansData } from "services";

// todo remove any for rejectvalue in future
export const getBeansThunk = createAsyncThunk<IBeansData, string, { rejectValue: any }>(
    "getBeansThunk",
    async (id, { rejectWithValue }) => {
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