<script lang="ts">
/* Grupos de afinidad datamined con filtro por puntos y lista expandible. */

import { store } from '$lib/state/store.svelte'
import { crearI18n } from '$lib/i18n'
import { displayName } from '$lib/domain/models'
import { colorDeRango } from '$lib/theme/theme'
import HeaderBar from '$lib/components/HeaderBar.svelte'

interface Props {
  onVolver: () => void
}

let { onVolver }: Props = $props()
const i18n = $derived(crearI18n(store.idioma))

const OPCIONES = [0, 2, 5, 7, 8]
let min = $state(0)
let grupoAbierto = $state<number | null>(null)

const grupos = $derived(store.modelo === null ? [] : store.modelo.todosLosGrupos().filter((g) => g.puntos >= min))

function claseDePuntos(puntos: number): string | null {
  if (puntos >= 20) return 'rank-great'
  if (puntos >= 10) return 'rank-good'
  if (puntos >= 4) return 'rank-fair'
  return null
}
</script>

<svelte:head>
  <title>{i18n.t('tab_groups')} · Uma Afinidad</title>
</svelte:head>

{#if store.modelo === null}
  <div class="centrado">{i18n.t('calculando')}</div>
{:else}
  <div class="pantalla">
    <HeaderBar titulo={i18n.t('tab_groups')} {onVolver} />

    <div class="chips-filtro" role="group" aria-label={i18n.t('filtro_todos')}>
      {#each OPCIONES as valor (valor)}
        <button
          type="button"
          class={valor === min ? 'filtro-chip activo' : 'filtro-chip'}
          aria-pressed={valor === min}
          onclick={() => (min = valor)}
        >
          {valor === 0 ? i18n.t('filtro_todos') : i18n.t('filtro_pt', valor)}
        </button>
      {/each}
    </div>

    {#if grupos.length === 0}
      <div class="centrado">{i18n.t('sin_grupos_filtro')}</div>
    {:else}
      <ul class="lista-grupos">
        {#each grupos as grupo (grupo.tipo)}
          {@const miembros = store.modelo.miembrosDeGrupo(grupo.tipo)}
          {@const abierto = grupoAbierto === grupo.tipo}
          <li>
            <article class={abierto ? 'card grupo abierto' : 'card grupo'}>
              <button
                type="button"
                class="grupo-cabecera"
                aria-expanded={abierto}
                aria-describedby={`estado-grupo-${grupo.tipo}`}
                onclick={() => (grupoAbierto = abierto ? null : grupo.tipo)}
              >
                <span id={`estado-grupo-${grupo.tipo}`} class="oculto-visualmente">
                  {i18n.t(abierto ? 'expandido' : 'contraido')}
                </span>
                <span class="grupo-id">#{grupo.tipo}</span>
                <span class="secundario grupo-cantidad">
                  {i18n.t('miembros_cantidad', miembros.length)}
                </span>
                <span
                  class="grupo-puntos"
                  style="color:{colorDeRango(claseDePuntos(grupo.puntos)) ?? 'var(--texto)'}"
                >
                  {grupo.puntos}pt
                </span>
              </button>
              {#if abierto}
                <div class="grupo-miembros">
                  {#each miembros as m (m.charId)}
                    <span class="miembro">{displayName(m, i18n.japones)}</span>
                  {/each}
                </div>
              {/if}
            </article>
          </li>
        {/each}
      </ul>
    {/if}
  </div>
{/if}
