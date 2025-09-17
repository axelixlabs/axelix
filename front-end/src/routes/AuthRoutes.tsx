import { Navigate, Route, Routes } from "react-router-dom";

import { MinimalLayout } from "../layout/MinimalLayout";
import { Login } from "../pages/Login";

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
