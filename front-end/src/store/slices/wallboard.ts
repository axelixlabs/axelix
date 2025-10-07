import { createAsyncThunk, createSlice, type PayloadAction } from "@reduxjs/toolkit";

import type { IServiceCardsData, IWallboardSliceState } from "models";
import { getWallboardData } from "services";

// import { getWallboardData } from "services";

const initialState: IWallboardSliceState = {
  loading: false,
  error: "",
  instances: [],
  filteredInstances: [],
  instancesSearchText: "",
};

// todo fix any in future
export const getWallboardDataThunk = createAsyncThunk<IServiceCardsData, void, { rejectValue: any }>(
  "getWallboardDataThunk",
  async (_, { rejectWithValue }) => {
    try {
      const response = await getWallboardData();

      return response.data;
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
      state.instancesSearchText = searchText;

      state.filteredInstances = state.instances.filter(({ name }) => {
        return name.toLowerCase().includes(searchText);
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
      state.instances = payload.instances;
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
