/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { lazy } from "react";
import { Navigate, Route, Routes } from "react-router-dom";

import Loadable from "components";
import { MainLayout } from "layout";

const GarbageCollector = Loadable(lazy(() => import("pages/GarbageCollector")));
const ScheduledTasks = Loadable(lazy(() => import("pages/ScheduledTasks")));
const Environment = Loadable(lazy(() => import("pages/Environment")));
const ConfigProps = Loadable(lazy(() => import("pages/ConfigProps")));
const Conditions = Loadable(lazy(() => import("pages/Conditions")));
const ThreadDump = Loadable(lazy(() => import("pages/ThreadDump")));
const Wallboard = Loadable(lazy(() => import("pages/Wallboard")));
const Dashboard = Loadable(lazy(() => import("pages/Dashboard")));
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
                <Route path="/dashboard" element={<Dashboard />} />
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
                <Route path="/instance/:instanceId/thread-dump" element={<ThreadDump />} />
                <Route path="/instance/:instanceId/garbage-collector" element={<GarbageCollector />} />
            </Route>
        </Routes>
    );
};
