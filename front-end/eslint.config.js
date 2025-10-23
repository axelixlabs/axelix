import js from "@eslint/js";

import react from "eslint-plugin-react";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";
import globals from "globals";
import tseslint from "typescript-eslint";
import eslintPluginPrettier from "eslint-plugin-prettier";
import eslintConfigPrettier from "eslint-config-prettier";

export default [
    {
        ignores: ["dist", "node_modules"],
    },
    {
        plugins: {
            "react-hooks": reactHooks,
            "react" : react,
            "react-refresh": reactRefresh,
            "prettier": eslintPluginPrettier,
        },
    },
    js.configs.recommended,
    ...tseslint.configs.recommended,
    {
        languageOptions: {
            globals: {
                ...globals.browser,
                ...globals.node,
                ...globals.es2022,
            },
        },
    },
    {
        files: ["**/*.{js, jsx, ts, tsx}"],
    },
    {
        settings: {
            "import/resolver": {
                typescript: {
                    project: "./tsconfig.json",
                },
            },
        },
    },
    eslintConfigPrettier,
    {
        // TODO: Remove this rule later on, once the error handling logic is resolved
        rules: {
            "@typescript-eslint/no-explicit-any": ["off"]
        }
    }
];
