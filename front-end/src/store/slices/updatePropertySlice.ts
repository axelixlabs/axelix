import { createSlice, } from "@reduxjs/toolkit";

import type { IUpdatePropertySliceState } from "models";
import { updatePropertyThunk } from "store/thunks";

const initialState: Omit<IUpdatePropertySliceState, "loading"> = {
    changePropertyloading: false,
    error: "",
    changePropertySuccess: false
};

export const UpdatePropertySlice = createSlice({
    name: "updatePropertySlice",
    initialState,
    reducers: {
        resetChangePropertySuccess: (state) => {
            state.changePropertySuccess = false
        }
    },
    extraReducers: (builder) => {
        builder.addCase(updatePropertyThunk.pending, (state) => {
            state.changePropertyloading = true;
            state.changePropertySuccess = false
        });
        builder.addCase(updatePropertyThunk.fulfilled, (state) => {
            state.changePropertyloading = false;
            state.changePropertySuccess = true
        });
        builder.addCase(updatePropertyThunk.rejected, (state, { payload }) => {
            const { status } = payload;
            state.changePropertySuccess = false

            state.changePropertyloading = false;
            if (status >= 400 && status < 500) {
                // todo translate this in future
                state.error = "Неизвестная ошибка";
            } else {
                state.error = "Произошла внутренняя ошибка сервиса";
            }
        });
    },
});

export const { resetChangePropertySuccess } = UpdatePropertySlice.actions

export default UpdatePropertySlice;
