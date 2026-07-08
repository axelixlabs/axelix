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
import { Navigate, Route, Routes } from "react-router";

import Loadable from "components";
import { useAppSelector } from "hooks";
import { MainLayout } from "layout";
import { DashboardSiderMenu } from "layout/siders";

const DashboardSpringFramework = Loadable(lazy(() => import("pages/Dashboard/DashboardSpringFramework")));
const DashboardPersistence = Loadable(lazy(() => import("pages/Dashboard/DashboardPersistence")));
const DashboardOverview = Loadable(lazy(() => import("pages/Dashboard/DashboardOverview")));
const DashboardJava = Loadable(lazy(() => import("pages/Dashboard/DashboardJava")));
const GarbageCollector = Loadable(lazy(() => import("pages/GarbageCollector")));
const ScheduledTasks = Loadable(lazy(() => import("pages/ScheduledTasks")));
const Transactional = Loadable(lazy(() => import("pages/Transactional")));
const Environment = Loadable(lazy(() => import("pages/Environment")));
const ConfigProps = Loadable(lazy(() => import("pages/ConfigProps")));
const UserProfile = Loadable(lazy(() => import("pages/UserProfile")));
const Conditions = Loadable(lazy(() => import("pages/Conditions")));
const ThreadDump = Loadable(lazy(() => import("pages/ThreadDump")));
const Wallboard = Loadable(lazy(() => import("pages/Wallboard")));
const Loggers = Loadable(lazy(() => import("pages/Loggers")));
const Details = Loadable(lazy(() => import("pages/Details")));
const Metrics = Loadable(lazy(() => import("pages/Metrics")));
const Caches = Loadable(lazy(() => import("pages/Caches")));
const Beans = Loadable(lazy(() => import("pages/Beans")));
const Users = Loadable(lazy(() => import("pages/Users")));
const MCP = Loadable(lazy(() => import("pages/MCP")));

export const MainRoutes = () => {
    const settings = useAppSelector((state) => state.settings);

    return (
        <>
            <Routes>
                <Route path="/" element={<MainLayout hideSider />}>
                    <Route index element={<Navigate to="/wallboard" replace />} />
                    <Route path="/wallboard" element={<Wallboard />} />
                    {settings.isMcpServerEnabled && <Route path="/mcp-server" element={<MCP />} />}
                    <Route path="*" element={<Navigate to="/wallboard" replace />} />
                </Route>

                <Route path="/dashboard" element={<MainLayout siderContent={<DashboardSiderMenu />} />}>
                    <Route index element={<Navigate to="overview" replace />} />
                    <Route path="overview" element={<DashboardOverview />} />
                    <Route path="java" element={<DashboardJava />} />
                    <Route path="persistence" element={<DashboardPersistence />} />
                    <Route path="spring-framework" element={<DashboardSpringFramework />} />
                </Route>

                <Route path="/" element={<MainLayout hideSider />}>
                    <Route path="/users" element={<Users />} />
                    <Route path="/users/:userId" element={<UserProfile />} />
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
