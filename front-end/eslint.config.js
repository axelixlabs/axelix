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
import js from "@eslint/js";

import eslintConfigPrettier from "eslint-config-prettier";
import licenseHeader from "eslint-plugin-header";
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
        },
    },
    {
        files: ["eslint.config.js"],
        rules: {
            complexity: ["off"],
        },
    },
    {
        settings: {
            react: { version: "detect" },
        },
    },
    eslintConfigPrettier,
];
