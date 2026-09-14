/* Sitio 100% estático: las rutas se prerenderizan y el estado vive en el
   cliente (localStorage + datos por fetch). */
export const prerender = true
export const ssr = false
export const trailingSlash = 'never'
