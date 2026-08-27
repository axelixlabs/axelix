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
import license from "rollup-plugin-license";
import { defineConfig, loadEnv } from "vite";
import svgr from "vite-plugin-svgr";

export default defineConfig(({ command }) => {
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
        build: {
            rollupOptions: {
                plugins:
                    command === "build"
                        ? [
                              license({
                                  thirdParty: {
                                      includePrivate: false,
                                      output: {
                                          file: path.resolve(
                                              __dirname,
                                              "reports/dependency-license/THIRD-PARTY-NOTICES.txt",
                                          ),
                                          template(dependencies) {
                                              const separator = "-".repeat(80);

                                              return dependencies
                                                  .map((dep) => {
                                                      const notice = dep.noticeText ? `\n\n${dep.noticeText}` : "";
                                                      const text = dep.licenseText ? `\n\n${dep.licenseText}` : "";

                                                      return `${dep.name}@${dep.version}\nLicense: ${dep.license}${text}${notice}`;
                                                  })
                                                  .join(`\n\n${separator}\n\n`);
                                          },
                                      },
                                  },
                              }),
                          ]
                        : [],
            },
        },
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
                "@": path.resolve(__dirname, "./src"),
            },
        },
    };
});
