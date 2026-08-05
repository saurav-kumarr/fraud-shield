import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  define: {
    global: 'window',
  },
  server: {
    // Mirrors what nginx does inside Docker, so the same relative-path
    // code ('' baseURL in api.js, '/ws' in WebSocketService.js) works
    // unchanged whether you run `npm run dev` locally or the full stack
    // in Docker - no environment-specific URLs anywhere in the app code.
    proxy: {
      '/api': 'http://localhost:8080',                       // API Gateway
      '/ws': { target: 'http://localhost:8083', ws: true },  // alert-service directly
    },
  },
})