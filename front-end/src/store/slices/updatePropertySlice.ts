import { createSlice, } from "@reduxjs/toolkit";

import { StatelessRequest } from "models";
import { updatePropertyThunk } from "store/thunks";

const initialState: StatelessRequest = StatelessRequest.inactive()

export const UpdatePropertySlice = createSlice({
    name: "updatePropertySlice",
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder.addCase(updatePropertyThunk.pending, () => {
          return StatelessRequest.loading()
        });
        builder.addCase(updatePropertyThunk.fulfilled, () => {
          return StatelessRequest.success()
        });
        builder.addCase(updatePropertyThunk.rejected, (state, { payload }) => {
            const { status } = payload;

            if (status >= 400 && status < 500) {
                // todo translate this in future
              return StatelessRequest.error("Неизвестная ошибка");
            } else {
              return StatelessRequest.error("Произошла внутренняя ошибка сервиса");
            }
        });
    },
});
