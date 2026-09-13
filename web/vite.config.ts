import { readFileSync } from 'node:fs'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const pkg = JSON.parse(readFileSync(new URL('./package.json', import.meta.url), 'utf8')) as {
  version: string
}

/* En GitHub Pages la web se publica como project site en /umafinidad/.
   En local (o si se publica en la raíz) queda en /. */
const base = process.env.VITE_BASE_URL ?? '/'

export default defineConfig({
  base,
  plugins: [react()],
  define: {
    __APP_VERSION__: JSON.stringify(pkg.version),
  },
  server: {
    port: 5173,
  },
  build: {
    target: 'es2022',
  },
})
