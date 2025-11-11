import { createSlice } from "@reduxjs/toolkit";

import { StatelessRequest } from "models";
import { updatePropertyThunk } from "store/thunks";

const initialState: StatelessRequest = StatelessRequest.inactive();

export const UpdatePropertySlice = createSlice({
    name: "updatePropertySlice",
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder.addCase(updatePropertyThunk.pending, () => {
            return StatelessRequest.loading();
        });
        builder.addCase(updatePropertyThunk.fulfilled, () => {
            return StatelessRequest.success();
        });
        builder.addCase(updatePropertyThunk.rejected, (_, { payload }) => {
            const { code } = payload;
            return StatelessRequest.error(code);
        });
    },
});
