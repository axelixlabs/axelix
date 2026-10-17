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

import { store } from "@/store";

import { AppRoutes } from "./routes";

import "./i18n/i18n";

export const App = () => {
    const theme: ThemeConfig = {
        // antd derives hover/active shades from these, so they must be literals; keep them equal to the :root ramps in index.css
        token: {
            colorPrimary: "#008743",
            colorSuccess: "#008743",
            colorError: "#d92d20",
            colorWarning: "#f79009",
            colorInfo: "#1570ef",
            colorLink: "#008743",
            colorText: "#101828",
            colorTextSecondary: "#475467",
            colorTextTertiary: "#667085",
            colorTextQuaternary: "#98a2b3",
            colorBorder: "#d0d5dd",
            colorBorderSecondary: "#eaecf0",
            // antd's generated tints of the primary green come out muddy, so the selected-item backgrounds are pinned to the green ramp
            controlItemBgActive: "#e6f7ee",
            controlItemBgActiveHover: "#ccefdd",
            fontFamily: "Golos, Helvetica, Arial, sans-serif",
            fontSize: 14,
            lineHeight: 1.5,
        },
        components: {
            Menu: {
                itemSelectedBg: "#e6f7ee",
                itemSelectedColor: "#007038",
            },
        },
    };

    return (
        <>
            <ConfigProvider
                theme={theme}
                tooltip={{
                    styles: {
                        root: {
                            maxWidth: 600,
                            whiteSpace: "normal",
                        },
                    },
                }}
                select={{
                    styles: {
                        root: {
                            minWidth: 120,
                        },
                    },
                }}
                breadcrumb={{
                    styles: {
                        root: {
                            marginBottom: 8,
                        },
                    },
                }}
                avatar={{
                    style: {
                        flexShrink: 0,
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
