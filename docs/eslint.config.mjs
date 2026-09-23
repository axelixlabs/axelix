import js from "@eslint/js";

import prettierConfig from "eslint-config-prettier";
import jsdoc from "eslint-plugin-jsdoc";
import prettier from "eslint-plugin-prettier";
import react from "eslint-plugin-react";
import reactHooks from "eslint-plugin-react-hooks";
import globals from "globals";
import tseslint from "typescript-eslint";

export default tseslint.config(
    { ignores: ["node_modules", ".next", ".source", "next-env.d.ts", "Дизайн документации"] },
    js.configs.recommended,
    tseslint.configs.recommended,
    {
        files: ["**/*.{ts,tsx,mts,mjs,js}"],
        languageOptions: {
            globals: { ...globals.browser, ...globals.node },
        },
        plugins: { react, "react-hooks": reactHooks, jsdoc, prettier },
        settings: { react: { version: "19.2" } },
        rules: {
            ...react.configs.flat.recommended.rules,
            ...reactHooks.configs.flat.recommended.rules,
            // the App Router has no React import and infers prop types from generated helpers
            "react/react-in-jsx-scope": "off",
            "react/prop-types": "off",
            "@typescript-eslint/naming-convention": [
                "error",
                { selector: "interface", format: ["PascalCase"], prefix: ["I"] },
                { selector: "enum", format: ["PascalCase"], prefix: ["E"] },
                { selector: "enumMember", format: ["UPPER_CASE"] },
            ],
            "jsdoc/lines-before-block": ["error", { lines: 1 }],
            complexity: ["error", 10],
            "max-depth": ["error", 2],
            "max-nested-callbacks": ["error", 2],
            "react/no-danger": "error",
            "react/jsx-no-target-blank": "error",
            "prettier/prettier": "error",
        },
    },
    {
        // `describe` > `it` > callback is three levels by construction
        files: ["src/tests/**"],
        rules: { "max-nested-callbacks": "off" },
    },
    prettierConfig,
);
