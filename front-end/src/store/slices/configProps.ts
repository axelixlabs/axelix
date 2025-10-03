import { createAsyncThunk, createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { IConfigPropsBeanData, IConfigPropsSliceState } from "models";
import { getConfigPropsData } from "services";

const initialState: IConfigPropsSliceState = {
  loading: false,
  error: "",
  beans: [],
  filteredBeans: [],
  configPropsSearchText: "",
};

export const getConfigPropsThunk = createAsyncThunk<IConfigPropsBeanData, string, { rejectValue: any }>(
  "getConfigPropsThunk",
  async (id, { rejectWithValue }) => {
    try {
      const response = await getConfigPropsData(id);

      return response.data;
    } catch (error: any) {
      return rejectWithValue({
        status: error.response?.status,
      });
    }
  });

export const ConfigPropsSlice = createSlice({
  name: "configPropsSlice",
  initialState,
  reducers: {
    filterConfigProps: (state, action: PayloadAction<string>) => {
      const searchText = action.payload.toLowerCase().trim();
      state.configPropsSearchText = searchText;

      state.filteredBeans = state.beans.filter((bean) => {
        const filterByBeanName = bean.beanName
          .toLowerCase()
          .includes(searchText);
        const filterByPrefix = bean.prefix.toLowerCase().includes(searchText);
        const filterByPropertiesName = bean.properties.some(({ key }) =>
          key.toLowerCase().includes(searchText)
        );

        return filterByBeanName || filterByPrefix || filterByPropertiesName;
      });
    },
  },
  extraReducers: (builder) => {
    builder.addCase(getConfigPropsThunk.pending, (state) => {
      state.loading = true;
    });
    builder.addCase(getConfigPropsThunk.fulfilled, (state, { payload }) => {
      state.loading = false;
      state.beans = payload.beans;
    });
    builder.addCase(getConfigPropsThunk.rejected, (state, { payload }: any) => {
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

export const { filterConfigProps } = ConfigPropsSlice.actions;

export default ConfigPropsSlice;
