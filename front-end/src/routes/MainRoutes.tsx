import { lazy } from "react";
import { Navigate, Route, Routes } from "react-router-dom";

import Loadable from "components";
import { MainLayout } from "layout";

const ScheduledTasks = Loadable(lazy(() => import("pages/ScheduledTasks")));
const Environment = Loadable(lazy(() => import("pages/Environment")));
const ConfigProps = Loadable(lazy(() => import("pages/ConfigProps")));
const Conditions = Loadable(lazy(() => import("pages/Conditions")));
const Wallboard = Loadable(lazy(() => import("pages/Wallboard")));
const Loggers = Loadable(lazy(() => import("pages/Loggers")));
const Details = Loadable(lazy(() => import("pages/Details")));
const Metrics = Loadable(lazy(() => import("pages/Metrics")));
const Caches = Loadable(lazy(() => import("pages/Caches")));
const Beans = Loadable(lazy(() => import("pages/Beans")));

export const MainRoutes = () => {
    return (
        <Routes>
            <Route path="/" element={<MainLayout hideSider />}>
                <Route index element={<Navigate to="/wallboard" replace />} />
                <Route path="/wallboard" element={<Wallboard />} />
                <Route path="*" element={<Navigate to="/wallboard" replace />} />
            </Route>

            <Route element={<MainLayout />}>
                <Route path="/instance/:instanceId/details" element={<Details />} />
                <Route path="/instance/:instanceId/metrics" element={<Metrics />} />
                <Route path="/instance/:instanceId/environment" element={<Environment />} />
                <Route path="/instance/:instanceId/beans" element={<Beans />} />
                <Route path="/instance/:instanceId/config-props" element={<ConfigProps />} />
                <Route path="/instance/:instanceId/loggers" element={<Loggers />} />
                <Route path="/instance/:instanceId/caches" element={<Caches />} />
                <Route path="/instance/:instanceId/scheduled-tasks" element={<ScheduledTasks />} />
                <Route path="/instance/:instanceId/conditions" element={<Conditions />} />
            </Route>
        </Routes>
    );
};
