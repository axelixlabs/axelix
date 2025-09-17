import { configureStore } from "@reduxjs/toolkit";

import { LoginSlice } from "./slices/login";

export const store = configureStore({
  reducer: {
    adminLogin: LoginSlice.reducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
