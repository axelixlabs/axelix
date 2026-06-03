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
import { defineConfig, globalIgnores } from "eslint/config";
import js from "@eslint/js";
import eslintConfigPrettier from "eslint-config-prettier";
import jsdoc from "eslint-plugin-jsdoc";
import eslintPluginPrettier from "eslint-plugin-prettier";
import globals from "globals";
import tseslint from "typescript-eslint";
import nextVitals from "eslint-config-next/core-web-vitals";
import nextTs from "eslint-config-next/typescript";
import licenseHeader from "@tony.ganchev/eslint-plugin-header";

export default defineConfig([
    globalIgnores([
        ".next/**",
        "out/**",
        "build/**",
        "next-env.d.ts",
        "node_modules",
    ]),

    js.configs.recommended,
    ...tseslint.configs.recommended,

    ...nextVitals,
    ...nextTs,

    {
        plugins: {
            prettier: eslintPluginPrettier,
            jsdoc: jsdoc,
            header: licenseHeader,
        },
    },

    {
        files: ["**/*.{js,jsx,ts,tsx}"],
        languageOptions: {
            parser: tseslint.parser,
            globals: {
                ...globals.browser,
                ...globals.node,
                ...globals.es2022,
            },
        },
        rules: {
            "prettier/prettier": "error",
            "jsdoc/lines-before-block": ["error", { lines: 1 }],
            "@typescript-eslint/naming-convention": [
                "error",
                {
                    selector: "interface",
                    format: ["PascalCase"],
                    prefix: ["I"],
                },
                {
                    selector: "enum",
                    format: ["PascalCase"],
                    prefix: ["E"],
                },
                {
                    selector: "enumMember",
                    format: ["UPPER_CASE"],
                },
            ],
            complexity: ["error", 10],
            "max-nested-callbacks": ["error", 2],
            "max-depth": ["error", 2],
            "react/no-danger": "error",
            "react/jsx-no-target-blank": "error",
            "react/no-unknown-property": "error",
            "react/jsx-curly-newline": ["error", "consistent"],
            "react/no-unstable-nested-components": "error",
            "header/header": ["error", "../LICENSE_HEADER"],
        },
    },

    eslintConfigPrettier,
]);