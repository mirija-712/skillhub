import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

/**
 * Dev : deux backends en parallèle.
 * - `/api` + `/storage` → Laravel (skillhub_back), ex. port 8000.
 * - `/auth-api` → Spring (authentification_back) sur 8080 ; le préfixe est réécrit en `/api` car l’app Spring
 *   expose déjà les routes sous `/api/...`.
 * Le front utilise `API_URL` et `AUTH_API_URL` dans `constants.js` pour cibler ces chemins relatifs.
 */
export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.js',
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8000',
        changeOrigin: true,
      },
      '/auth-api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/auth-api/, '/api'),
      },
      '/storage': {
        target: 'http://localhost:8000',
        changeOrigin: true,
      },
    },
  },
})
