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
import type { IColorPallete } from "models";

export const SEARCH_PARAMS_FILTER = "f";
export const UNKNOWN_ERROR = "UNKNOWN_ERROR";
export const IS_AUTH = "isAuth";

export const colorPalette: Record<string, IColorPallete> = {
    GREY: {
        colorPrimary: "#838383",
        colorPrimaryHover: "#d9d9d9",
        colorPrimaryActive: "#595959",
    },
    RED: {
        colorPrimary: "#ff4d4f",
        colorPrimaryHover: "#ff7875",
        colorPrimaryActive: "#d9363e",
    },
    ORANGE: {
        colorPrimary: "#faad14",
        colorPrimaryHover: "#ffd666",
        colorPrimaryActive: "#d48806",
    },
    YELLOW: {
        colorPrimary: "#fadb14",
        colorPrimaryHover: "#ffec3d",
        colorPrimaryActive: "#d4b106",
    },
    BLUE: {
        colorPrimary: "#1890ff",
        colorPrimaryHover: "#69c0ff",
        colorPrimaryActive: "#096dd9",
    },
    LIGHT_BLUE: {
        colorPrimary: "#13c2c2",
        colorPrimaryHover: "#36e3e3",
        colorPrimaryActive: "#0b8a8a",
    },
    GREEN: {
        colorPrimary: "#52c41a",
        colorPrimaryHover: "#95de64",
        colorPrimaryActive: "#389e0d",
    },
    PURPLE: {
        colorPrimary: "#722ed1",
        colorPrimaryHover: "#b37feb",
        colorPrimaryActive: "#531dab",
    },
    WHITE: {
        colorPrimary: "#ffffff",
        colorPrimaryHover: "#e6e6e6",
        colorPrimaryActive: "#cccccc",
    },
};
