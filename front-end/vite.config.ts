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
            "/api/external": {
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
            output: {
                /* TODO: Vite/Rollup detected a circular dependency inside Recharts (modules re-exporting each other)
                   If Recharts modules end up in different chunks, the build can brea
                   So we forced all Recharts code into one chunk
                   This is a temporary fix 
                */
                manualChunks(id) {
                    if (id.includes("node_modules/recharts")) {
                        return "recharts";
                    }
                },
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
        },
    },
});
