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
import { MinimalLayout } from "layout";

const Login = Loadable(lazy(() => import("pages/Login")));

export const AuthRoutes = () => {
    return (
        <>
            <Routes>
                <Route path="/" element={<Navigate to="/login" />} />

                <Route path="/login" element={<MinimalLayout />}>
                    <Route index element={<Login />} />
                </Route>

                <Route path="*" element={<Navigate to="/login" />} />
            </Routes>
        </>
    );
};
