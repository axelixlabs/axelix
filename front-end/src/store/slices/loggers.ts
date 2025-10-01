import {
  createAsyncThunk,
  createSlice,
  type PayloadAction,
} from "@reduxjs/toolkit";

import type { ILoggersSliceState } from "models";

const initialState: ILoggersSliceState = {
  loading: false,
  levels: [],
  loggers: [],
  loggersSearchText: "",
  filteredLoggers: [],
  error: "",
};

export const setLoggerLevelThunk = createAsyncThunk(
  "setLoggerLevelThunk",
  // todo Удалить _ и заменить реальными параметрами в будущем, когда будет выполнен запрос с реальными данными.
  async (_: any, { dispatch, rejectWithValue }) => {
    try {
      // искусственная задержка вместо реального запроса
      await new Promise((resolve) => setTimeout(resolve, 500));

      dispatch(getLoggersThunk(""));

      // todo fix this in future
      return {};
    } catch (error: any) {
      return rejectWithValue({
        status: error.response?.status,
      });
    }
  }
);

export const getLoggersThunk = createAsyncThunk(
  "getLoggersThunk",
  // todo Удалить _ и заменить реальными параметрами в будущем, когда будет выполнен запрос с реальными данными.
  async (_: any, { rejectWithValue }) => {
    try {
      // fix this after creating the endpoint
      // const response = await getEnvironmentData(id);
      // todo Удалить any и заменить реальными типами в будущем.
      const response: any = await new Promise((resolve) => {
        setTimeout(() => {
          resolve({
            data: {
              levels: ["OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE"],
              loggers: [
                {
                  name: "ROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOTROOT",
                  configuredLevel: "OFF",
                  effectiveLevel: "OFF",
                },
                { name: "vtik", configuredLevel: "", effectiveLevel: "OFF" },
                {
                  name: "hvgck",
                  configuredLevel: "WARN",
                  effectiveLevel: "WARN",
                },
                {
                  name: "nzr.rhpg.fgji",
                  configuredLevel: "",
                  effectiveLevel: "WARN",
                },
                {
                  name: "uyrgdo.ffxsxv.hzhc",
                  configuredLevel: "ERROR",
                  effectiveLevel: "ERROR",
                },
                {
                  name: "odau",
                  configuredLevel: "",
                  effectiveLevel: "ERROR",
                },
                {
                  name: "suh.zur",
                  configuredLevel: "INFO",
                  effectiveLevel: "INFO",
                },
                {
                  name: "clco",
                  configuredLevel: "",
                  effectiveLevel: "INFO",
                },
                {
                  name: "pubw.wzq.phawiu",
                  configuredLevel: "DEBUG",
                  effectiveLevel: "DEBUG",
                },
                {
                  name: "pubw.wzq.phawiu",
                  configuredLevel: "",
                  effectiveLevel: "DEBUG",
                },
                {
                  name: "pubw.wzq.phawiu",
                  configuredLevel: "TRACE",
                  effectiveLevel: "TRACE",
                },
                {
                  name: "pubw.wzq.phawiu",
                  configuredLevel: "",
                  effectiveLevel: "TRACE",
                },
              ],
            },
          });
        }, 500);
      });

      return response.data;
    } catch (error: any) {
      return rejectWithValue({
        status: error.response?.status,
      });
    }
  }
);

export const LoggersSlice = createSlice({
  name: "loggers",
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
    });
    builder.addCase(setLoggerLevelThunk.fulfilled, (state) => {
      state.loading = false;
    });
    // fix this in future
    builder.addCase(setLoggerLevelThunk.rejected, (state, { payload }: any) => {
      const { status } = payload;
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
