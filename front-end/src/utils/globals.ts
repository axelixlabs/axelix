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
import type { IColorPallete } from "models";

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
