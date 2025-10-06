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
