import { lazy } from "react";
import { Navigate, Route, Routes } from "react-router-dom";

import { MainLayout } from "layout";
import Loadable from "components";

const Environment = Loadable(lazy(() => import('pages/Environment')))
const ConfigProps = Loadable(lazy(() => import('pages/ConfigProps')))
const Wallboard = Loadable(lazy(() => import('pages/Wallboard')))
const Loggers = Loadable(lazy(() => import('pages/Loggers')))
const Beans = Loadable(lazy(() => import('pages/Beans')))

export const MainRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<MainLayout />}>
        <Route index element={<Navigate to="wallboard" replace/>} />

        <Route path="instance/:instanceId/environment" element={<Environment />} />
        <Route path="instance/:instanceId/beans" element={<Beans />} />
        <Route path="instance/:instanceId/config-props" element={<ConfigProps />} />
        <Route path="instance/:instanceId/loggers" element={<Loggers />} />

        <Route path="wallboard" element={<Wallboard />} />
        <Route path="*" element={<Navigate to="/wallboard" replace/>} />
      </Route>
    </Routes>
  );
};
