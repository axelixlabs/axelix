import { createAsyncThunk } from "@reduxjs/toolkit";

import type { IServiceCardsData } from "models";
import { getWallboardData } from "services";

// todo fix any in future
export const getWallboardDataThunk = createAsyncThunk<IServiceCardsData, void, { rejectValue: any }>(
    "getWallboardDataThunk",
    async (_, { rejectWithValue }) => {
        try {
            const response = await getWallboardData();

            return response.data;
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    },
);
