import { configureStore } from "@reduxjs/toolkit";

import { LoginSlice, UpdatePropertySlice } from "./slices";

export const store = configureStore({
    reducer: {
        login: LoginSlice.reducer,
        updateProperty: UpdatePropertySlice.reducer,
    },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
