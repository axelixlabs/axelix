import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";

import type { IEnvironmentData, IEnvironmentSliceState } from "../../models";

const initialState: IEnvironmentSliceState = {
  loading: false,
  error: "",
  data: {
    activeProfiles: [],
    defaultProfiles: [],
    propertySources: [],
  },
};

export const environmentThunk = createAsyncThunk(
  "environment",
  async (id: string, { rejectWithValue }) => {
    try {
      // fix this after creating the endpoint
      // const response = await getEnvironmentData(id);
      const response: { data: IEnvironmentData } = await new Promise(
        (resolve) => {
          setTimeout(() => {
            resolve({
              data: {
                activeProfiles: ["prod", "tarantool", "postgres"],
                defaultProfiles: [],
                propertySources: [
                  {
                    name: "server.ports",
                    properties: [{ key: "local.server.port", value: "8080" }],
                  },
                  {
                    name: "bootstrapProperties-vault:business-cards-processing,postgres",
                    properties: [
                      {
                        key: "spring.datasource.driverClassName",
                        value: "org.postgresql.Driver",
                      },
                      {
                        key: "spring.datasource.driverClassName",
                        value: "org.postgresql.Driver",
                      },
                      {
                        key: "spring.datasource.driverClassName",
                        value: "org.postgresql.Driver",
                      },
                    ],
                  },
                  {
                    name: "bootstrapProperties-vault:business-cards-processing,postgres",
                    properties: [
                      {
                        key: "spring.datasource.driverClassName",
                        value: "org.postgresql.Driver",
                      },
                      {
                        key: "spring.datasource.driverClassName",
                        value: "org.postgresql.Driver",
                      },
                    ],
                  },
                ],
              },
            });
          }, 1000);
        }
      );

      return response.data;
    } catch (error: any) {
      return rejectWithValue({
        status: error.response?.status,
      });
    }
  }
);

export const EnvironmentSlice = createSlice({
  name: "environment",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder.addCase(environmentThunk.pending, (state) => {
      state.loading = true;
    });
    builder.addCase(environmentThunk.fulfilled, (state, { payload }) => {
      state.loading = false;
      state.data = payload;
    });
    builder.addCase(environmentThunk.rejected, (state, { payload }: any) => {
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

export default EnvironmentSlice.reducer;
