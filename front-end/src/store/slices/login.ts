/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { createSlice } from "@reduxjs/toolkit";

import type { ILoginSliceState } from "models";
import { loginThunk } from "store/thunks";

const initialState: ILoginSliceState = {
    loading: false,
    accessToken: localStorage.getItem("accessToken"),
    error: "",
};

export const LoginSlice = createSlice({
    name: "login",
    initialState,
    reducers: {
        logout: (state) => {
            state.accessToken = null;
        },
    },
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

export const { logout } = LoginSlice.actions;
