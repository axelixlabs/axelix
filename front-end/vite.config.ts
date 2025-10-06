import * as path from "path";
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { visualizer } from 'rollup-plugin-visualizer';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
  },
  build: {
    rollupOptions: {
      plugins: [
        visualizer({
          filename: 'dist/bundle-stats.html',
          title: 'Bundle Visualizer',
          template: 'treemap',
          open: false,
          gzipSize: true,
          brotliSize: true
        })
      ]
    }
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
    },
  },
});
