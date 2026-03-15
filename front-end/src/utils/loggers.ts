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
import type { TabsProps } from "antd";
import type { TFunction } from "i18next";

import { ELoggersTabs, type IColorPallete } from "models";

import { colorPalette } from "./globals";

export const loggersColors: Record<string, IColorPallete> = {
    OFF: colorPalette.GREY,
    ERROR: colorPalette.RED,
    WARN: colorPalette.ORANGE,
    INFO: colorPalette.YELLOW,
    DEBUG: colorPalette.BLUE,
    TRACE: colorPalette.LIGHT_BLUE,
    ALL: colorPalette.GREEN,
    DEFAULT: colorPalette.PURPLE,
};

export const loggersTabs = (t: TFunction): TabsProps["items"] => {
    return [
        {
            key: ELoggersTabs.LOGGERS,
            label: t("Loggers.loggers"),
        },
        {
            key: ELoggersTabs.LOGGER_GROUPS,
            label: t("Loggers.loggerGroups"),
        },
    ];
};
