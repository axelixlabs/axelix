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
import { useEffect, useState } from "react";
import { BrowserRouter } from "react-router";

import { getCookie } from "helpers";
import { useAppDispatch } from "hooks";
import { getAxelixSettings } from "services";
import { setAxelixSettings } from "store/slices";

import { AuthRoutes } from "./AuthRoutes";
import { MainRoutes } from "./MainRoutes";

export const AppRoutes = () => {
    // authorities cookie is supposed to live as long as the main access cookie,
    // so we rely on the presence of the authorities cookie here
    const isAuthenticated = Boolean(getCookie("authorities"));
    const dispatch = useAppDispatch();
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        getAxelixSettings()
            .then((value) => {
                dispatch(setAxelixSettings(value.data));
                setLoading(false);
            })
            .catch((reason) => {
                // TODO: Insert an image of something went wrong
                return reason;
            });
    }, []);

    if (loading) {
        return null;
    }

    return (
        <>
            <BrowserRouter>{isAuthenticated ? <MainRoutes /> : <AuthRoutes />}</BrowserRouter>
        </>
    );
};
