<script lang="ts">
/* Mejores linajes: top 20 de linajes completos con cálculo diferido
   (worker). */

import { goto } from '$app/navigation'
import { resolve } from '$app/paths'
import { store } from '$lib/state/store.svelte'
import { crearI18n } from '$lib/i18n'
import { Async } from '$lib/state/async.svelte'
import { pedirTopLinajes } from '$lib/worker/cliente'
import HeaderBar from '$lib/components/HeaderBar.svelte'
import CardFilaTop from '$lib/components/CardFilaTop.svelte'

const i18n = $derived(crearI18n(store.idioma))
const top = new Async(() => pedirTopLinajes(20))

$effect(() => {
  if (store.modelo !== null) void top.ejecutar()
})
</script>

<svelte:head>
  <title>{i18n.t('tab_top')} · Uma Afinidad</title>
</svelte:head>

{#if store.modelo === null || top.datos === null}
  <div class="pantalla">
    <HeaderBar titulo={i18n.t('tab_top')} />
    <div class="centrado">{i18n.t('calculando')}</div>
  </div>
{:else if top.datos.length === 0}
  <div class="pantalla">
    <HeaderBar titulo={i18n.t('tab_top')} />
    <div class="centrado">{i18n.t('sin_datos')}</div>
  </div>
{:else}
  <div class="pantalla">
    <HeaderBar
      titulo={i18n.t('tab_top')}
      pillTexto={`${top.datos.length} ${i18n.t('tab_top').toLowerCase()}`}
    />
    <ul class="lista-top">
      {#each top.datos as combo, i (`${combo.hijo.charId}-${i}`)}
        <li>
          <CardFilaTop
            {i}
            {combo}
            modelo={store.modelo}
            onVerHerencia={() => {
              store.cargarLinaje(combo)
              void goto(resolve('/compat'))
            }}
          />
        </li>
      {/each}
    </ul>
  </div>
{/if}
