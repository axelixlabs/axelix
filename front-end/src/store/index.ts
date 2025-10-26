import { configureStore } from "@reduxjs/toolkit";

import {
    CachesSlice,
    ConfigPropsSlice,
    EnvironmentSlice,
    LoginSlice,
    UpdatePropertySlice,
    WallboardSlice,
} from "./slices";

export const store = configureStore({
    reducer: {
        login: LoginSlice.reducer,
        environment: EnvironmentSlice.reducer,
        configProps: ConfigPropsSlice.reducer,
        wallboard: WallboardSlice.reducer,
        updateProperty: UpdatePropertySlice.reducer,
        caches: CachesSlice.reducer,
    },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
