import babel from "@rolldown/plugin-babel";
import react, { reactCompilerPreset } from "@vitejs/plugin-react";
import * as path from "path";
import { defineConfig, loadEnv } from "vite";
import svgr from "vite-plugin-svgr";

export default defineConfig(() => {
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
                "@": path.resolve(import.meta.dirname, "../../master/front-end/src"),
            },
        },
    };
});