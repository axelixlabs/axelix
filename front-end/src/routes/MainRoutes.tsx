import { Navigate, Route, Routes } from "react-router-dom";

import { ConfigProps, Environment, Beans, Loggers, Wallboard } from "pages";
import { MainLayout } from "layout";

export const MainRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<MainLayout />}>
        <Route index element={<>1</>} />
        <Route path="environment" element={<Environment />} />
        <Route path="beans" element={<Beans />} />
        <Route path="config-props" element={<ConfigProps />} />
        <Route path="loggers" element={<Loggers />} />
      </Route>

      <Route path="/wallboard" element={<MainLayout hideSider/>}>
        <Route index element={<Wallboard />} />
      </Route>
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
};
