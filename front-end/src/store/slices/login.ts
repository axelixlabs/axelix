import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";

import type { ILoginSliceState, ILoginSubmitValue } from "models";
import { login } from "services/auth";

const initialState: ILoginSliceState = {
    loading: false,
    accessToken: localStorage.getItem("accessToken"),
    error: "",
};

export const loginThunk = createAsyncThunk("login", async (data: ILoginSubmitValue, { rejectWithValue }) => {
    try {
        // todo do this after resolving cors error from server
        const response = await login(data);
        const accessToken = response.headers.Authorization;

        return Promise.resolve({ accessToken });
    } catch (error: any) {
        return rejectWithValue({
            status: error.response?.status,
        });
    }
});

export const LoginSlice = createSlice({
    name: "login",
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder.addCase(loginThunk.pending, (state) => {
            state.loading = true;
        });

        builder.addCase(loginThunk.fulfilled, (state, { payload }) => {
            const accessToken = payload.accessToken;
            localStorage.setItem("accessToken", accessToken);
            state.accessToken = accessToken;
            state.loading = false;
            window.location.href = "/";
        });

        builder.addCase(loginThunk.rejected, (state, { payload }: any) => {
            // todo fix in future
            const { status } = payload;

            if (status === 400 || status === 401) {
                state.error = "Неправильный логин или пароль";
            } else if (status === 429) {
                state.error = "Слишком много попыток входа";
            } else if (status >= 400 && status < 500) {
                state.error = "Неизвестная ошибка";
            } else {
                state.error = "Произошла внутренняя ошибка сервиса";
            }

            state.loading = false;
        });
    },
});

export default LoginSlice;
