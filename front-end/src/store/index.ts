import { configureStore } from "@reduxjs/toolkit";

import { LoginSlice, EnvironmentSlice, BeansSlice } from "./slices";

export const store = configureStore({
  reducer: {
    login: LoginSlice.reducer,
    environment: EnvironmentSlice.reducer,
    beans: BeansSlice.reducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
