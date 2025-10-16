import { createSlice } from "@reduxjs/toolkit";

import { setLoggerLevelThunk } from "store/thunks";
import type { ILoggersSliceState } from "models";

const initialState: ILoggersSliceState = {
  loading: false,
  updateLoggerSuccess: false,
  levels: [],
  loggers: [],
  error: "",
};

export const LoggersSlice = createSlice({
  name: "loggersSlice",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder.addCase(setLoggerLevelThunk.pending, (state) => {
      state.loading = true;
      state.updateLoggerSuccess = false;
    });
    builder.addCase(setLoggerLevelThunk.fulfilled, (state) => {
      state.loading = false;
      state.updateLoggerSuccess = true
    });
    // todo fix this in future
    builder.addCase(setLoggerLevelThunk.rejected, (state, { payload }: any) => {
      const { status } = payload;
      state.updateLoggerSuccess = false;
      state.loading = false;

      if (status >= 400 && status < 500) {
        // todo translate this in future
        state.error = "Неизвестная ошибка";
      } else {
        state.error = "Произошла внутренняя ошибка сервиса";
      }
    });
  },
});

export default LoggersSlice;
