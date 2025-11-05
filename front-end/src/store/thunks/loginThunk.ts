import { createAsyncThunk } from "@reduxjs/toolkit";

import type { ILoginSubmitRequestData } from "models";
import { login } from "services";

// TODO: Remove any in future
export const loginThunk = createAsyncThunk<any, ILoginSubmitRequestData, { rejectValue: any }>(
    "login",
    async (data, { rejectWithValue }) => {
        try {
            const response = await login(data);
            const accessToken = response.headers.Authorization;

            return Promise.resolve({ accessToken });
        } catch (error: any) {
            return rejectWithValue({
                status: error.response?.status,
            });
        }
    },
);
