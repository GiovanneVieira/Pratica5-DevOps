import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// No Docker o proxy precisa apontar para o serviço da API (http://api:8080);
// fora dele (npm run dev na host) o padrão é localhost:8080.
const apiTarget = process.env.API_PROXY_TARGET || 'http://localhost:8080'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: apiTarget,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
