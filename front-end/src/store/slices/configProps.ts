import { createAsyncThunk, createSlice, type PayloadAction, type WritableDraft } from "@reduxjs/toolkit";
import type { IConfigPropsBean, IConfigPropsBeanData, IConfigPropsSliceState, IUpdatePropertyData } from "models";
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

    localeUpdateConfigPropsProperty: (state, action: PayloadAction<IUpdatePropertyData>) => {
      const { propertySourceName, propertyName, newValue } = action.payload;

      const changePropertyValue = (beans: WritableDraft<IConfigPropsBean>[]) => {
        const findedBean = beans.find(ps => ps.beanName === propertySourceName);
        if (findedBean) {
          const findedProperty = findedBean.properties.find(p => p.key === propertyName);
          if (findedProperty) {
            findedProperty.value = newValue;
          }
        }
      }

      changePropertyValue(state.beans)
      changePropertyValue(state.filteredBeans)
    }
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

export const { filterConfigProps, localeUpdateConfigPropsProperty } = ConfigPropsSlice.actions;

export default ConfigPropsSlice;
