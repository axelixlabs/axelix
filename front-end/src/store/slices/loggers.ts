import { createAsyncThunk, createSlice, type PayloadAction } from "@reduxjs/toolkit";

import type { ILoggerData, ILoggersSliceState, ISetLoggerLevelRequestData } from "models";
import { getLoggersData, setLoggerLevel } from "services/loggers";



const initialState: ILoggersSliceState = {
  loading: false,
  updateLoggerSuccess: false,
  levels: [],
  loggers: [],
  loggersSearchText: "",
  filteredLoggers: [],
  error: "",
};

// todo fix any in future for rejectValue
export const setLoggerLevelThunk = createAsyncThunk<void, ISetLoggerLevelRequestData, { rejectValue: any }>(
  "setLoggerLevelThunk",
  async ({ id, loggerName, loggingLevel }, { dispatch, rejectWithValue }) => {
    try {
      await setLoggerLevel(id, loggerName, loggingLevel)
      dispatch(getLoggersThunk("56019718-3b84-4ecd-9b84-287754dbd7d4"));
    } catch (error: any) {
      return rejectWithValue({
        status: error.response?.status,
      });
    }
  }
);

// todo fix any in future for rejectValue
export const getLoggersThunk = createAsyncThunk<ILoggerData, string, { rejectValue: any }>(
  "getLoggersThunk",
  async (id, { rejectWithValue }) => {
    try {
      const response = await getLoggersData(id);

      return response.data;
    } catch (error: any) {
      return rejectWithValue({
        status: error.response?.status,
      });
    }
  }
);

export const LoggersSlice = createSlice({
  name: "loggersSlice",
  initialState,
  reducers: {
    filterLoggers: (state, action: PayloadAction<string>) => {
      const searchText = action.payload.toLowerCase().trim();
      state.loggersSearchText = searchText;

      state.filteredLoggers = state.loggers.filter((logger) => {
        return logger.name.toLowerCase().includes(searchText);
      });
    },
  },
  extraReducers: (builder) => {
    builder.addCase(getLoggersThunk.pending, (state) => {
      state.loading = true;
    });
    builder.addCase(getLoggersThunk.fulfilled, (state, { payload }) => {
      state.loading = false;
      state.levels = payload.levels;
      state.loggers = payload.loggers;
    });
    builder.addCase(getLoggersThunk.rejected, (state, { payload }: any) => {
      const { status } = payload;

      state.loading = false;
      if (status >= 400 && status < 500) {
        // todo translate this in future
        state.error = "Неизвестная ошибка";
      } else {
        state.error = "Произошла внутренняя ошибка сервиса";
      }
    });

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

export const { filterLoggers } = LoggersSlice.actions;

export default LoggersSlice;
