import { configureStore } from "@reduxjs/toolkit";

import { LoginSlice, EnvironmentSlice } from "./slices";

export const store = configureStore({
  reducer: {
    login: LoginSlice.reducer,
    environment: EnvironmentSlice.reducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
