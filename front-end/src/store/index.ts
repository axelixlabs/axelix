import { configureStore } from "@reduxjs/toolkit";

import {
  LoginSlice,
  EnvironmentSlice,
  BeansSlice,
  ConfigPropsSlice,
  LoggersSlice,
  WallboardSlice,
  UpdatePropertySlice,
} from "./slices";

export const store = configureStore({
  reducer: {
    login: LoginSlice.reducer,
    environment: EnvironmentSlice.reducer,
    beans: BeansSlice.reducer,
    configProps: ConfigPropsSlice.reducer,
    loggers: LoggersSlice.reducer,
    wallboard: WallboardSlice.reducer,
    updateProperty: UpdatePropertySlice.reducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
