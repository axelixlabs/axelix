import { BrowserRouter } from "react-router-dom";

import { useAppSelector } from "hooks";

import { AuthRoutes } from "./AuthRoutes";
import { MainRoutes } from "./MainRoutes";

export const AppRoutes = () => {
    const token = useAppSelector((state) => {
        return state.login.accessToken;
    });

    return <BrowserRouter>{!token ? <AuthRoutes /> : <MainRoutes />}</BrowserRouter>;
};
