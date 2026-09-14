import adapter from '@sveltejs/adapter-static'
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte'

/* En GitHub Pages el sitio se publica como project site en /umafinidad/.
   En local (o publicado en la raíz) queda en /. */
const base = process.env.BASE_PATH ?? ''

export default {
  preprocess: vitePreprocess({ script: true }),
  kit: {
    adapter: adapter({ fallback: '404.html' }),
    paths: { base },
  },
}
