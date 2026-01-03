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
