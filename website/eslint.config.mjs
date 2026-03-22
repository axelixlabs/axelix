import nextVitals from "eslint-config-next/core-web-vitals";
import nextTs from "eslint-config-next/typescript";
import eslintConfigPrettier from "eslint-config-prettier";
import eslintPluginPrettier from "eslint-plugin-prettier";
import { defineConfig, globalIgnores } from "eslint/config";

const eslintConfig = defineConfig([
    ...nextVitals,
    ...nextTs,

    // Override default ignores of eslint-config-next.
    globalIgnores([".next/**", "out/**", "build/**", "next-env.d.ts"]),

    eslintConfigPrettier,

    {
        plugins: {
            prettier: eslintPluginPrettier,
        },
        rules: {
            "prettier/prettier": "error",
        },
    },
]);

export default eslintConfig;
