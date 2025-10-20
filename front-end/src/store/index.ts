import { configureStore } from "@reduxjs/toolkit";

import {
  LoginSlice,
  EnvironmentSlice,
  ConfigPropsSlice,
  LoggersSlice,
  WallboardSlice,
  UpdatePropertySlice,
  CachesSlice,
  ScheduledTasksSlice
} from "./slices";

export const store = configureStore({
  reducer: {
    login: LoginSlice.reducer,
    environment: EnvironmentSlice.reducer,
    configProps: ConfigPropsSlice.reducer,
    loggers: LoggersSlice.reducer,
    wallboard: WallboardSlice.reducer,
    updateProperty: UpdatePropertySlice.reducer,
    caches: CachesSlice.reducer,
    scheduledTasks: ScheduledTasksSlice.reducer
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
