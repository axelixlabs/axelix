import { lazy } from "react";
import { Navigate, Route, Routes } from "react-router-dom";

import Loadable from "components";
import { MinimalLayout } from "layout";

const Login = Loadable(lazy(() => import("pages/Login")));

export const AuthRoutes = () => {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/login" />} />

            <Route path="/login" element={<MinimalLayout />}>
                <Route index element={<Login />} />
            </Route>

            <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
    );
};
