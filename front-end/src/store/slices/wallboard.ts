import { createAsyncThunk, createSlice, type PayloadAction } from "@reduxjs/toolkit";

import type { IServiceCardsData, IWallboardSliceState } from "models";

// import { getWallboardData } from "services";

const initialState: IWallboardSliceState = {
  loading: false,
  error: "",
  serviceCards: [],
  filteredServiceCards: [],
  serviceCardsSearchText: "",
};

// todo fix any in future
export const getWallboardDataThunk = createAsyncThunk<IServiceCardsData, void, { rejectValue: any }>(
  "getWallboardDataThunk",
  async (_, { rejectWithValue }) => {
    try {
      // const response = await getWallboardData();

      // return response.data;

      return {
        applications: [
          {
            springBootVersion: "3.5.1",
            javaVersion: "21",
            status: "UP",
            serviceName: "123axile-api-feature-service-petclinic",
            serviceVersion: "1.32.14 SNAPSHOT",
            commitHash: "K2357",
          },
          {
            springBootVersion: "12.5.1",
            javaVersion: "20",
            status: "DOWN",
            serviceName: "345axile-api-feature-service-petclinic-petclinic",
            serviceVersion: "1.32.12",
            commitHash: "K2347",
          },
          {
            springBootVersion: "12.5.1",
            javaVersion: "20",
            status: "DOWN",
            serviceName: "678axile-api-feature-service-petclinic-petclinic",
            serviceVersion: "1.32.12",
            commitHash: "K2347",
          },
          {
            springBootVersion: "12.5.1",
            javaVersion: "20",
            status: "UNKNOWN",
            serviceName: "123axile-api-feature-service-petclinic-asdzcx",
            serviceVersion: "1.32.12",
            commitHash: "K2347",
          },
          {
            springBootVersion: "12.5.1",
            javaVersion: "20",
            status: "DOWN",
            serviceName: "123axile-api-feature-service-petclinic-vcv",
            serviceVersion: "1.32.12",
            commitHash: "K2347",
          },
          {
            springBootVersion: "12.5.1",
            javaVersion: "20",
            status: "DOWN",
            serviceName: "555axile-api-feature-service-petclinic-petclinic",
            serviceVersion: "1.32.12",
            commitHash: "K2347",
          },
          {
            springBootVersion: "12.5.1",
            javaVersion: "20",
            status: "DOWN",
            serviceName: "555axile-api-feature",
            serviceVersion: "1.32.12",
            commitHash: "K2347",
          },
          {
            springBootVersion: "12.5.1",
            javaVersion: "20",
            status: "UNKNOWN",
            serviceName: "555ppppaxile-api-feature-service-petclinic-petclinic",
            serviceVersion: "1.32.12",
            commitHash: "K2347",
          },
          {
            springBootVersion: "12.5.1",
            javaVersion: "20",
            status: "UNKNOWN",
            serviceName: "667axile-api-feature-service-petclinic-petclinic",
            serviceVersion: "1.32.12",
            commitHash: "K2347",
          },
          {
            springBootVersion: "12.5.1",
            javaVersion: "20",
            status: "UP",
            serviceName: "555axile-api-feature1",
            serviceVersion: "1.32.12",
            commitHash: "K2347",
          },
          {
            springBootVersion: "12.5.1",
            javaVersion: "20",
            status: "DOWN",
            serviceName: "555axile-api.feature.service.petclinic.petclinic",
            serviceVersion: "1.32.12",
            commitHash: "K2347",
          },
        ],
      };
    } catch (error: any) {
      return rejectWithValue({
        status: error.response?.status,
      });
    }
  });

export const WallboardSlice = createSlice({
  name: "wallboardSlice",
  initialState,
  reducers: {
    filterServiceCards: (state, action: PayloadAction<string>) => {
      const searchText = action.payload.toLowerCase().trim();
      state.serviceCardsSearchText = searchText;

      state.filteredServiceCards = state.serviceCards.filter(
        ({ serviceName }) => {
          return serviceName.toLowerCase().includes(searchText);
        }
      );
    },
  },
  extraReducers: (builder) => {
    builder.addCase(getWallboardDataThunk.pending, (state) => {
      state.loading = true;
    });
    builder.addCase(getWallboardDataThunk.fulfilled, (state, { payload }) => {
      state.loading = false;
      state.serviceCards = payload.applications;
    });
    builder.addCase(getWallboardDataThunk.rejected, (state, { payload }: any) => {
      const { status } = payload;

      state.loading = false;
      if (status >= 400 && status < 500) {
        // todo translate this in future
        state.error = "Неизвестная ошибка";
      } else {
        state.error = "Произошла внутренняя ошибка сервиса";
      }
    }
    );
  },
});

export const { filterServiceCards } = WallboardSlice.actions;

export default WallboardSlice;
