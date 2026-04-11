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
import babel from "@rolldown/plugin-babel";
import react, { reactCompilerPreset } from "@vitejs/plugin-react";

import * as path from "path";
import { defineConfig, loadEnv } from "vite";
import svgr from "vite-plugin-svgr";

export default defineConfig(() => {
    // we have to load the .env file manually here since vite interprets the .env after the config getting loaded
    const env = loadEnv("", process.cwd(), "");
    const apiTarget = env.VITE_LOCAL_API_URL ?? "http://localhost:8080";

    return {
        plugins: [
            react(),
            babel({
                presets: [reactCompilerPreset()],
            }),
            svgr(),
        ],
        server: {
            port: 3000,
            proxy: {
                "/api/external": {
                    target: apiTarget,
                    changeOrigin: true,
                    secure: false,
                },
            },
        },
        resolve: {
            alias: {
                src: path.resolve(__dirname, "./src"),
                api: path.resolve(__dirname, "./src/api"),
                assets: path.resolve(__dirname, "./src/assets"),
                components: path.resolve(__dirname, "./src/components"),
                hooks: path.resolve(__dirname, "./src/hooks"),
                layout: path.resolve(__dirname, "./src/layout"),
                models: path.resolve(__dirname, "./src/models"),
                pages: path.resolve(__dirname, "./src/pages"),
                services: path.resolve(__dirname, "./src/services"),
                store: path.resolve(__dirname, "./src/store"),
                utils: path.resolve(__dirname, "./src/utils"),
                helpers: path.resolve(__dirname, "./src/helpers"),
                "enterprise-assets": path.resolve(__dirname, "./src/enterprise/src/assets"),
                "enterprise-components": path.resolve(__dirname, "./src/enterprise/src/components"),
                "enterprise-helpers": path.resolve(__dirname, "./src/enterprise/src/helpers"),
                "enterprise-models": path.resolve(__dirname, "./src/enterprise/src/models"),
                "enterprise-utils": path.resolve(__dirname, "./src/enterprise/src/utils"),
            },
        },
    };
});
