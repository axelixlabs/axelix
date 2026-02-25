/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import { lazy } from "react";
import { Navigate, Route, Routes } from "react-router-dom";

import Loadable from "components";
import { MainLayout } from "layout";

const GarbageCollector = Loadable(lazy(() => import("pages/GarbageCollector")));
const ScheduledTasks = Loadable(lazy(() => import("pages/ScheduledTasks")));
const Transactional = Loadable(lazy(() => import("pages/Transactional")));
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
        <>
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
                    <Route path="/instance/:instanceId/transactional" element={<Transactional />} />
                </Route>
            </Routes>
        </>
    );
};
