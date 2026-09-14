<script lang="ts">
/* Shell de la web: cabecera con navegación real, contenido y pie.
   Aplica tema, tamaño de texto y negrita del usuario, e inicializa el
   store con los datos datamined. */

import { onMount, type Snippet } from 'svelte'
import { page } from '$app/state'
import { resolve } from '$app/paths'
import { store } from '$lib/state/store.svelte'
import { crearI18n } from '$lib/i18n'
import { resolverTema } from '$lib/theme/theme'
import { ESCALA_TAMANO } from '$lib/data/prefs'
import Icono from '$lib/components/Icono.svelte'
import SnackbarHost from '$lib/components/SnackbarHost.svelte'
import BienvenidaAccesibilidad from '$lib/components/BienvenidaAccesibilidad.svelte'
import '../lib/theme/base.css'
import '../lib/theme/identidad.css'

interface Props {
  children: Snippet
}

let { children }: Props = $props()

const i18n = $derived(crearI18n(store.idioma))
let sistemaOscuro = $state(false)
const tema = $derived(resolverTema(store.tema, sistemaOscuro))

const NAV = [
  { ruta: '/compat', icono: 'corazon', clave: 'tab_compat' },
  { ruta: '/top', icono: 'top', clave: 'tab_top' },
  { ruta: '/corredora', icono: 'corredora', clave: 'tab_corredora' },
  { ruta: '/elenco', icono: 'elenco', clave: 'tab_elenco' },
  { ruta: '/ajustes', icono: 'ajustes', clave: 'tab_mas' },
] as const

type RutaNav = '/compat' | '/top' | '/corredora' | '/elenco' | '/ajustes'

function activa(ruta: RutaNav): boolean {
  return page.url.pathname === resolve(ruta)
}

onMount(() => {
  const media = window.matchMedia('(prefers-color-scheme: dark)')
  sistemaOscuro = media.matches
  const onChange = (evento: MediaQueryListEvent) => {
    sistemaOscuro = evento.matches
  }
  media.addEventListener('change', onChange)
  void store.init()
  return () => media.removeEventListener('change', onChange)
})

$effect(() => {
  document.documentElement.dataset.theme = tema
  document.documentElement.style.setProperty(
    '--escala-texto',
    String(ESCALA_TAMANO[store.tamanoTexto] ?? 1),
  )
  document.body.classList.toggle('negrita', store.textoNegrita)
  document.documentElement.lang = i18n.codigo
})
</script>

<SnackbarHost>
  <div class="site-shell">
    <header class="site-header">
      <div class="site-header-inner">
        <a class="marca" href={resolve('/compat')}>
          <span class="marca-punto" aria-hidden="true"></span>
          Uma Afinidad
        </a>
        <nav class="site-nav" aria-label={i18n.t('app_name')}>
          {#each NAV as item (item.ruta)}
            <a href={resolve(item.ruta)} aria-current={activa(item.ruta) ? 'page' : undefined}>
              <Icono nombre={item.icono} />
              <span>{i18n.t(item.clave)}</span>
            </a>
          {/each}
        </nav>
      </div>
    </header>

    <main class="site-main">
      {@render children()}
    </main>

    <footer class="site-footer">
      <p>
        <a href={resolve('/grupos')}>{i18n.t('tab_groups')}</a>
        ·
        <a href={resolve('/ranking')}>{i18n.t('tab_ranking')}</a>
        ·
        <a href={resolve('/ranking-padres')}>{i18n.t('ranking_padres_titulo')}</a>
      </p>
    </footer>

    {#if store.mostrarBienvenida}
      <BienvenidaAccesibilidad />
    {/if}
  </div>
</SnackbarHost>
