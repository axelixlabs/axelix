import { Navigate, Route, Routes } from "react-router-dom";

import { DashboardLayout } from "layout";
import { Environment } from "pages";
import { Beans } from "pages";

export const MainRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<DashboardLayout />}>
        <Route index element={<>1</>} />
        <Route path="environment" element={<Environment />} />
        <Route path="beans" element={<Beans />} />
      </Route>
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
};
