import { configureStore } from "@reduxjs/toolkit";

import { LoginSlice, EnvironmantSlice } from "./slices";

export const store = configureStore({
  reducer: {
    login: LoginSlice.reducer,
    environmant: EnvironmantSlice.reducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
