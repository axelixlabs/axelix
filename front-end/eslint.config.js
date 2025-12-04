import js from "@eslint/js";

import eslintConfigPrettier from "eslint-config-prettier";
import eslintPluginJsonc from "eslint-plugin-jsonc";
import eslintPluginPrettier from "eslint-plugin-prettier";
import react from "eslint-plugin-react";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";
import globals from "globals";
import jsoncParser from "jsonc-eslint-parser";
import tseslint from "typescript-eslint";

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
        },
    },
    {
        settings: {
            react: { version: "detect" },
        },
    },
    eslintConfigPrettier,
];
