import {
  createAsyncThunk,
  createSlice,
  type PayloadAction,
} from "@reduxjs/toolkit";

import type { IEnvironmentData, IEnvironmentSliceState } from "../../models";

const initialState: IEnvironmentSliceState = {
  loading: false,
  error: "",
  // todo remove data field in future
  activeProfiles: [],
  defaultProfiles: [],
  propertySources: [],
  environmentSearchText: "",
  filteredEnvironments: [],
};

export const environmentThunk = createAsyncThunk(
  "environment",
  // todo fix this after creating the endpoint
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
                    name: "bootstrapProperties1",
                    properties: [
                      {
                        key: "spring.datasource.driverClassName1",
                        value: "org.postgresql.Driver",
                      },
                      {
                        key: "spring.datasource.driverClassName2",
                        value: "org.postgresql.Driver",
                      },
                      {
                        key: "spring.datasource.driverClassName3",
                        value: "org.postgresql.Driver",
                      },
                    ],
                  },
                  {
                    name: "bootstrapProperties2",
                    properties: [
                      {
                        key: "spring.datasource.driverClassName4",
                        value: "org.postgresql.Driver",
                      },
                      {
                        key: "spring.datasource.driverClassName5",
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
  reducers: {
    filterEnvironments: (state, action: PayloadAction<string>) => {
      const searchText = action.payload.toLowerCase().trim();
      state.environmentSearchText = searchText;

      state.filteredEnvironments = state.propertySources.filter(
        ({ name, properties }) => {
          const filterByPropertySourcesName = name
            .toLowerCase()
            .includes(searchText);
          const filterByPropertiesName = properties.some(({ key }) =>
            key.toLowerCase().includes(searchText)
          );
          return filterByPropertySourcesName || filterByPropertiesName;
        }
      );
    },
  },
  extraReducers: (builder) => {
    builder.addCase(environmentThunk.pending, (state) => {
      state.loading = true;
    });
    builder.addCase(environmentThunk.fulfilled, (state, { payload }) => {
      state.loading = false;
      state.activeProfiles = payload.activeProfiles;
      state.defaultProfiles = payload.defaultProfiles;
      state.propertySources = payload.propertySources;
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

export const { filterEnvironments } = EnvironmentSlice.actions;

export default EnvironmentSlice;
