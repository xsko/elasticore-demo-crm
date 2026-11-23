import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath } from 'node:url';

// The screens are bundled into the server's static folder, so the jar serves them from
// the same origin as the API (the session cookie and the XSRF token need that). In
// development the dev server forwards /api to the running server.
const api = process.env.CRM_API ?? 'http://localhost:18090';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@gen': fileURLToPath(new URL('../src/main/ts', import.meta.url)),
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: { '/api': { target: api, changeOrigin: false } },
    fs: { allow: ['..'] },
  },
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
    chunkSizeWarningLimit: 2500,
  },
});
