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
import { palette, typography } from "@/theme/tokens";

import { AppRoutes } from "./routes";

import "./i18n/i18n";

export const App = () => {
    const theme: ThemeConfig = {
        token: {
            colorPrimary: palette.green[600],
            colorSuccess: palette.green[600],
            colorError: palette.red[600],
            colorWarning: palette.orange[500],
            colorInfo: palette.blue[600],
            colorLink: palette.green[600],
            colorText: palette.grey[900],
            colorTextSecondary: palette.grey[600],
            colorTextTertiary: palette.grey[500],
            colorTextQuaternary: palette.grey[400],
            colorBorder: palette.grey[300],
            colorBorderSecondary: palette.grey[200],
            // antd's generated tints of the primary green come out muddy, so the selected-item backgrounds are pinned to the green ramp
            controlItemBgActive: palette.green[50],
            controlItemBgActiveHover: palette.green[100],
            fontFamily: typography.fontFamily,
            fontFamilyCode: typography.fontFamilyCode,
            fontSize: typography.fontSize.body,
            lineHeight: typography.lineHeight.normal,
        },
        components: {
            // Menus are rendered only in the siders, whose own background shows through the items
            Menu: {
                itemBg: "transparent",
                subMenuItemBg: "transparent",
                activeBarBorderWidth: 0,
                itemSelectedBg: palette.green[50],
                itemSelectedColor: palette.green[700],
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
