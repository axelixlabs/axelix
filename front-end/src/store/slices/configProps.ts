import {
  createAsyncThunk,
  createSlice,
  type PayloadAction,
} from "@reduxjs/toolkit";
import type { IConfigPropsBean, IConfigPropsSliceState } from "models";

const initialState: IConfigPropsSliceState = {
  loading: false,
  error: "",
  beans: [],
  filteredConfigProps: [],
  configPropsSearchText: "",
};

export const getConfigProps = createAsyncThunk(
  "getConfigProps",
  // todo Удалить _ и заменить реальными параметрами в будущем, когда будет выполнен запрос с реальными данными.
  async (_: any, { rejectWithValue }) => {
    try {
      // fix this after creating the endpoint
      // const response = await getEnvironmentData(id);
      const response: { data: IConfigPropsBean[] } = await new Promise(
        (resolve) => {
          setTimeout(() => {
            resolve({
              data: [
                {
                  beanName: "BeanName1",
                  prefix: "prefix1",
                  properties: [
                    {
                      key: "key1",
                      value: "value1",
                    },
                    {
                      key: "key2",
                      value: "value2",
                    },
                    {
                      key: "key3",
                      value: "value3",
                    },
                  ],
                },
                {
                  beanName: "BeanName2",
                  prefix: "prefix2",
                  properties: [
                    {
                      key: "key4",
                      value: "value4",
                    },
                    {
                      key: "key5",
                      value: "value5",
                    },
                    {
                      key: "key6",
                      value: "value6",
                    },
                  ],
                },
                {
                  beanName: "BeanName3",
                  prefix: "prefix3",
                  properties: [
                    {
                      key: "key7",
                      value: "value7",
                    },
                    {
                      key: "key8",
                      value: "value8",
                    },
                    {
                      key: "key9",
                      value: "value9",
                    },
                    {
                      key: "key10",
                      value: "value10",
                    },
                  ],
                },
                {
                  beanName: "BeanName4",
                  prefix: "prefix4",
                  properties: [
                    {
                      key: "key11",
                      value: "value11",
                    },
                  ],
                },
              ],
            });
          }, 500);
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

export const ConfigPropsSlice = createSlice({
  name: "configProps",
  initialState,
  reducers: {
    filterConfigProps: (state, action: PayloadAction<string>) => {
      const searchText = action.payload.toLowerCase().trim();
      state.configPropsSearchText = searchText;

      state.filteredConfigProps = state.beans.filter((bean) => {
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
    builder.addCase(getConfigProps.pending, (state) => {
      state.loading = true;
    });
    builder.addCase(getConfigProps.fulfilled, (state, { payload }) => {
      state.loading = false;
      state.beans = payload;
    });
    builder.addCase(getConfigProps.rejected, (state, { payload }: any) => {
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
