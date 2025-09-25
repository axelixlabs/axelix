import {
  createAsyncThunk,
  createSlice,
  type PayloadAction,
} from "@reduxjs/toolkit";

import type { IBean, IBeansSliceData } from "models";

const initialState: IBeansSliceData = {
  loading: false,
  error: "",
  beans: [],
  beansSearchText: "",
  filteredBeans: [],
};

export const getBeansThunk = createAsyncThunk(
  "getBeans",
  // todo use this id in real requests
  async (id: string, { rejectWithValue }) => {
    try {
      // fix this after creating the endpoint
      // const response = await getEnvironmantData(id);
      const response: { beans: IBean[] } = await new Promise((resolve) => {
        setTimeout(() => {
          resolve({
            beans: [
              {
                beanName: "beanName1",
                scope: "postgres",
                className: "111",
                aliases: ["aliase1", "aliase2", "aliase3", "aliase4"],
                dependencies: [
                  "beanName2",
                  "beanName91wvq",
                  "beanName4ejovneoj",
                ],
              },
              {
                beanName: "beanName2",
                scope: "postgres",
                className: "222",
                aliases: [],
                dependencies: ["beanName4", "beanName3", "beanName9"],
              },
              {
                beanName: "beanName3",
                scope: "postgres",
                className:
                  "org.springframewoagark.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: ["beanName6"],
              },
              {
                beanName: "beanName4",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: [],
              },
              {
                beanName: "beanName5",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: [],
              },
              {
                beanName: "beanName6",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: ["beanName7"],
              },
              {
                beanName: "beanName7",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: ["beanName8"],
              },
              {
                beanName: "beanName8",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: ["beanName9"],
              },
              {
                beanName: "beanName9",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: ["beanName1", "beanName2"],
              },
              {
                beanName: "beanName21",
                scope: "postgres",
                className: "111",
                aliases: ["aliase1", "aliase2", "aliase3", "aliase4"],
                dependencies: ["beanName2", "beanName3"],
              },
              {
                beanName: "beanName223",
                scope: "postgres",
                className: "222",
                aliases: [],
                dependencies: ["beanName4", "beanName3", "beanName9"],
              },
              {
                beanName: "beanName3423",
                scope: "postgres",
                className:
                  "org.springframewoagark.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: ["beanName6"],
              },
              {
                beanName: "beanName4234523",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: [],
              },
              {
                beanName: "beanName5ffwf",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: [],
              },
              {
                beanName: "beanName6asfsaf",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: ["beanName7"],
              },
              {
                beanName: "beanName7sfaga",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: ["beanName8"],
              },
              {
                beanName: "beanName8cawcw",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: ["beanName9"],
              },
              {
                beanName: "beanName9afwqfw",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: ["beanName1", "beanName2"],
              },
              {
                beanName: "beanName31",
                scope: "postgres",
                className: "111",
                aliases: ["aliase1", "aliase2", "aliase3", "aliase4"],
                dependencies: ["beanName2", "beanName3"],
              },
              {
                beanName: "beanName2ijvuien",
                scope: "postgres",
                className: "222",
                aliases: [],
                dependencies: ["beanName4", "beanName3", "beanName9"],
              },
              {
                beanName: "beanName3wnvejnv",
                scope: "postgres",
                className:
                  "org.springframewoagark.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: ["beanName6"],
              },
              {
                beanName: "beanName4ejovneoj",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: [],
              },
              {
                beanName: "beanName5zxczxc",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: [],
              },
              {
                beanName: "beanName4ejovnasdeoj",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: ["beanName2"],
              },
              {
                beanName: "beanName7vqvw",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: ["beanName2"],
              },
              {
                beanName: "beanName8qwvqw",
                scope: "postgres",
                className:
                  "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration",
                aliases: [],
                dependencies: ["beanName2"],
              },
              {
                beanName: "beanName91wvq",
                scope: "postgres",
                className: "xyz",
                aliases: [],
                dependencies: ["beanName1", "beanName2"],
              },
            ],
          });
        }, 500);
      });

      return response.beans;
      // todo fix any type after receiving real data from backend
    } catch (error: any) {
      return rejectWithValue({
        status: error.response?.status,
      });
    }
  }
);

export const BeansSlice = createSlice({
  name: "beansSlice",
  initialState,
  reducers: {
    filterBeans: (state, action: PayloadAction<string>) => {
      const searchText = action.payload.toLowerCase().trim();
      state.beansSearchText = searchText;

      state.filteredBeans = state.beans.filter((bean) => {
        const filterByBeanName = bean.beanName
          .toLowerCase()
          .includes(searchText);

        const filterByClassName = bean.className
          .toLowerCase()
          .includes(searchText);

        const filterByAliases = bean.aliases.some((alias) =>
          alias.toLowerCase().includes(searchText)
        );

        return filterByBeanName || filterByClassName || filterByAliases;
      });
    },
  },
  extraReducers: (builder) => {
    builder.addCase(getBeansThunk.pending, (state) => {
      state.loading = true;
    });
    builder.addCase(getBeansThunk.fulfilled, (state, { payload }) => {
      state.loading = false;
      state.beans = payload;
    });
    // todo fix any type after receiving real data from backend
    builder.addCase(getBeansThunk.rejected, (state, { payload }: any) => {
      const { status } = payload;

      state.loading = false;
      // todo change the logic in the future if needed, and also add a translation
      if (status >= 400 && status < 500) {
        state.error = "Неизвестная ошибка";
      } else {
        state.error = "Произошла внутренняя ошибка сервиса";
      }
    });
  },
});

export const { filterBeans } = BeansSlice.actions;

export default BeansSlice.reducer;
