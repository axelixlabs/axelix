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
import { App as AntdApp, ConfigProvider, type ThemeConfig } from "antd";
import { Provider } from "react-redux";

import { store } from "store";

import { AppRoutes } from "./routes";

import "./i18n/i18n";

export const App = () => {
    const theme: ThemeConfig = {
        token: {
            colorPrimary: "#00ab55",
            fontFamily: "Golos, Helvetica, Arial, sans-serif",
            fontSize: 15,
            lineHeight: 1.5,
        },
    };

    return (
        <>
            <ConfigProvider
                theme={theme}
                tooltip={{
                    styles: {
                        root: {
                            maxWidth: "600px",
                            whiteSpace: "normal",
                        },
                    },
                }}
            >
                <AntdApp>
                    <Provider store={store}>
                        <AppRoutes />
                    </Provider>
                </AntdApp>
            </ConfigProvider>
        </>
    );
};
