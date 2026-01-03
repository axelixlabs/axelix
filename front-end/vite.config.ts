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
import react from "@vitejs/plugin-react";

import * as path from "path";
import { visualizer } from "rollup-plugin-visualizer";
import { defineConfig } from "vite";
import svgr from "vite-plugin-svgr";

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        react({
            babel: {
                plugins: ["babel-plugin-react-compiler"],
            },
        }),
        svgr(),
    ],
    server: {
        port: 3000,
        proxy: {
            "/api/axelix": {
                target: "http://158.160.200.59",
                changeOrigin: true,
                secure: false,
            },
        },
    },
    build: {
        rollupOptions: {
            plugins: [
                visualizer({
                    filename: "dist/bundle-stats.html",
                    title: "Bundle Visualizer",
                    template: "treemap",
                    open: false,
                    gzipSize: true,
                    brotliSize: true,
                }),
            ],
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
        },
    },
});
