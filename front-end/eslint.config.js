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
import js from "@eslint/js";

import eslintConfigPrettier from "eslint-config-prettier";
import licenseHeader from "eslint-plugin-header";
import jsdoc from "eslint-plugin-jsdoc";
import eslintPluginJsonc from "eslint-plugin-jsonc";
import eslintPluginPrettier from "eslint-plugin-prettier";
import react from "eslint-plugin-react";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";
import globals from "globals";
import jsoncParser from "jsonc-eslint-parser";
import tseslint from "typescript-eslint";

// See https://github.com/Stuk/eslint-plugin-header/issues/57
licenseHeader.rules.header.meta.schema = false;

export default [
    {
        ignores: ["dist", "node_modules"],
    },
    {
        plugins: {
            "react-hooks": reactHooks,
            react: react,
            "react-refresh": reactRefresh,
            prettier: eslintPluginPrettier,
            jsonc: eslintPluginJsonc,
            header: licenseHeader,
            jsdoc: jsdoc,
        },
    },
    js.configs.recommended,
    ...tseslint.configs.recommended,
    {
        files: ["**/*.json"],
        languageOptions: {
            parser: jsoncParser,
        },
        rules: {
            "jsonc/indent": ["error", 4],
            "jsonc/comma-dangle": ["error", "never"],
            "jsonc/quote-props": ["error", "always"],
            "jsonc/quotes": ["error", "double"],
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
            // TODO: Remove this rule later on, once the error handling logic is resolved
            "@typescript-eslint/no-explicit-any": ["off"],
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
            "header/header": ["error", "../LICENSE_HEADER"],
            complexity: ["error", 10],
            "max-nested-callbacks": ["error", 2],
            "max-depth": ["error", 2],
            "react/no-danger": "error",
            "react/jsx-no-target-blank": "error",
            "react/no-unknown-property": "error",
            "react/jsx-curly-newline": ["error", "consistent"],
            "react/no-unstable-nested-components": "error",
        },
    },
    {
        files: ["eslint.config.js"],
        rules: {
            complexity: ["off"],
        },
    },
    {
        files: ["cypress/**"],
        rules: {
            "max-nested-callbacks": ["off"],
        },
    },
    {
        settings: {
            react: { version: "detect" },
        },
    },
    eslintConfigPrettier,
];
