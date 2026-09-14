<script lang="ts">
/* Panel de resultado de compatibilidad (total + vínculos por sección). */

import type { AffinityModel } from '../domain/affinity'
import { displayName } from '../domain/models'
import { store } from '../state/store.svelte'
import { crearI18n } from '../i18n'
import { colorDeRango, fondoDeRango } from '../theme/theme'
import RankPill from './RankPill.svelte'
import { EstadoSeccion, type FilaVinculoUi, type ResultadoCompat } from '../state/resultado'

interface Props {
  modelo: AffinityModel
  res: ResultadoCompat
  japones: boolean
  idTitulo?: string
}

let { modelo, res, japones, idTitulo = 'resultado-titulo' }: Props = $props()
const i18n = $derived(crearI18n(store.idioma))
const fondoTotal = $derived(fondoDeRango(res.rangoTotal?.clase) ?? 'var(--superficie-alta)')
const colorTotal = $derived(colorDeRango(res.rangoTotal?.clase) ?? 'var(--texto)')

function nombres(v: FilaVinculoUi): string {
  return v.ids
    .map((id) => {
      const c = modelo.porId(id)
      return c ? displayName(c, japones) : String(id)
    })
    .join(' × ')
}

function notaDeEstado(estado: EstadoSeccion): string | null {
  switch (estado) {
    case EstadoSeccion.FALTA_HIJO:
      return i18n.t('falta_hijo')
    case EstadoSeccion.ELIGE_PADRE:
      return i18n.t('elige_un_padre')
    case EstadoSeccion.OTRO_PADRE:
      return i18n.t('elegi_otro_padre')
    case EstadoSeccion.FALTAN_PADRES:
      return i18n.t('faltan_padres')
    case EstadoSeccion.SIN_ABUELOS:
      return i18n.t('no_hay_abuelos')
    default:
      return null
  }
}
</script>

{#if res.vacio}
  <p class="nota">{i18n.t('elegi_hijo_empezar')}</p>
{:else}
  <div class="resultado-panel">
    <div class="total-caja" style="background:{fondoTotal}">
      <h2 id={idTitulo} class="total-titulo">{i18n.t('total_herencia')}</h2>
      <span class="total-numero" style="color:{colorTotal}">
        {res.rangoTotal ? `${res.rangoTotal.simbolo} ` : ''}{res.total ?? 0}
      </span>
    </div>

    {#each [
      { titulo: i18n.t('sec_hijo_padres'), filas: res.hijoPadres, estado: res.estadoHijoPadres },
      {
        titulo: i18n.t('sec_entre_padres'),
        filas: res.entrePadres ? [res.entrePadres] : [],
        estado: res.estadoEntrePadres,
      },
      {
        titulo: i18n.t('sec_hijo_padres_abuelos'),
        filas: res.hijoPadreAbuelos,
        estado: res.estadoHijoPadreAbuelos,
      },
    ] as seccion (seccion.titulo)}
      <section class="seccion-vinculos">
        <h3>{seccion.titulo}</h3>
        {#if seccion.estado === EstadoSeccion.CON_FILAS}
          {#each seccion.filas as fila (fila.ids.join('-'))}
            <article class="fila-vinculo">
              <div class="fila-vinculo-cabecera">
                <span class="fila-vinculo-nombres">{nombres(fila)}</span>
                <RankPill rango={fila.rango} puntos={fila.puntos} />
              </div>
              {#if fila.esCorredora}
                <p class="nota compacta">{i18n.t('corredora_nota')}</p>
              {/if}
            </article>
          {/each}
        {:else}
          <p class="nota">{notaDeEstado(seccion.estado)}</p>
        {/if}
      </section>
    {/each}

    {#if res.notaSinHijo}<p class="nota">{i18n.t('sin_hijo_completa')}</p>{/if}
  </div>
{/if}
