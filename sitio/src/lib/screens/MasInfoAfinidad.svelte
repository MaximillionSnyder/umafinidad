<script lang="ts">
/* Panel "Más info" de Mi corredora: complementa la pantalla con el desglose
   del total por vínculos, la explicación del reparto de las filas y las
   mejores alternativas por slot. Es SOLO lectura: no altera la selección ni
   ningún cálculo. */

import type { AffinityModel } from '$lib/domain/affinity'
import type { Seleccion } from '$lib/domain/herencia'
import type { ResultadoCompat } from '$lib/state/resultado'
import { displayName } from '$lib/domain/models'
import { store } from '$lib/state/store.svelte'
import { crearI18n } from '$lib/i18n'
import {
  RankFair,
  RankGreat,
  VinculoEntrePadres,
  VinculoHijoPadres,
  VinculoTrios,
} from '$lib/theme/theme'
import RankPill from '$lib/components/RankPill.svelte'

interface Props {
  modelo: AffinityModel
  seleccion: Seleccion
  japones: boolean
  res: ResultadoCompat | null
  onCerrar: () => void
}

let { modelo, seleccion, japones, res, onCerrar }: Props = $props()
const i18n = $derived(crearI18n(store.idioma))

const ETIQUETAS_ROL = [
  'rol_hijo',
  'rol_padre1',
  'rol_padre2',
  'rol_abuelo1_p1',
  'rol_abuelo2_p1',
  'rol_abuelo1_p2',
  'rol_abuelo2_p2',
]

const subtotalHijoPadres = $derived(res?.hijoPadres.reduce((suma, f) => suma + f.puntos, 0) ?? 0)
const subtotalEntrePadres = $derived(res?.entrePadres?.puntos ?? 0)
const subtotalTrios = $derived(res?.hijoPadreAbuelos.reduce((suma, f) => suma + f.puntos, 0) ?? 0)
const total = $derived(res?.total ?? 0)

/* Mejor alternativa de cada slot editable, con el total resultante. */
type Cambio = { slot: number; nombre: string; total: number }
const cambios = $derived.by<Cambio[]>(() => {
  if (seleccion.length !== 7 || seleccion.some((v) => v === null)) return []
  const salida: Cambio[] = []
  for (let slot = 1; slot <= 6; slot++) {
    const alt = modelo.alternativasParaSlot(seleccion, slot, 1)[0]
    if (alt !== undefined) {
      salida.push({ slot, nombre: displayName(alt.personaje, japones), total: alt.total })
    }
  }
  return salida
})

function nombresDe(ids: number[]): string {
  return ids
    .map((id) => {
      const personaje = modelo.porId(id)
      return personaje ? displayName(personaje, japones) : String(id)
    })
    .join(' × ')
}
</script>

<div class="mas-info-afinidad">
  <h2 id="mas-info-titulo">{i18n.t('afinidad_info_titulo')}</h2>
  <p class="nota compacta">{i18n.t('afinidad_info_intro')}</p>

  {#if res !== null && !res.vacio}
    <div class="barra-composicion" aria-hidden="true">
      {#if subtotalHijoPadres > 0}
        <span style="flex:{subtotalHijoPadres};background:{VinculoHijoPadres}"></span>
      {/if}
      {#if subtotalEntrePadres > 0}
        <span style="flex:{subtotalEntrePadres};background:{VinculoEntrePadres}"></span>
      {/if}
      {#if subtotalTrios > 0}
        <span style="flex:{subtotalTrios};background:{VinculoTrios}"></span>
      {/if}
    </div>

    <ul class="leyenda-vinculos">
      <li>
        <span class="punto" style="background:{VinculoHijoPadres}"></span>
        <span class="leyenda-nombre">{i18n.t('sec_hijo_padres')}</span>
        <span class="leyenda-subtotal">{subtotalHijoPadres} pt</span>
      </li>
      <li>
        <span class="punto" style="background:{VinculoEntrePadres}"></span>
        <span class="leyenda-nombre">{i18n.t('sec_entre_padres')}</span>
        <span class="leyenda-subtotal">{subtotalEntrePadres} pt</span>
      </li>
      <li>
        <span class="punto" style="background:{VinculoTrios}"></span>
        <span class="leyenda-nombre">{i18n.t('sec_hijo_padres_abuelos')}</span>
        <span class="leyenda-subtotal">{subtotalTrios} pt</span>
      </li>
    </ul>

    {#each [
      { titulo: i18n.t('sec_hijo_padres'), color: VinculoHijoPadres, subtotal: subtotalHijoPadres, filas: res.hijoPadres },
      { titulo: i18n.t('sec_entre_padres'), color: VinculoEntrePadres, subtotal: subtotalEntrePadres, filas: res.entrePadres ? [res.entrePadres] : [] },
      { titulo: i18n.t('sec_hijo_padres_abuelos'), color: VinculoTrios, subtotal: subtotalTrios, filas: res.hijoPadreAbuelos },
    ] as grupo (grupo.titulo)}
      {#if grupo.filas.length > 0}
        <section class="seccion-vinculos">
          <div class="grupo-vinculos-cabecera">
            <h3 style="color:{grupo.color}">{grupo.titulo}</h3>
            <span class="leyenda-subtotal">{grupo.subtotal} pt</span>
          </div>
          {#each grupo.filas as fila (fila.ids.join('-'))}
            <article class="fila-vinculo">
              <div class="fila-vinculo-cabecera">
                <span class="fila-vinculo-nombres">{nombresDe(fila.ids)}</span>
                <RankPill rango={fila.rango} puntos={fila.puntos} />
              </div>
              {#if fila.esCorredora}
                <p class="nota compacta">{i18n.t('corredora_nota')}</p>
              {/if}
            </article>
          {/each}
        </section>
      {/if}
    {/each}

    <p class="ecuacion-afinidad">
      {subtotalHijoPadres} + {subtotalEntrePadres} + {subtotalTrios} = {total}
    </p>
  {/if}

  <p class="nota compacta">{i18n.t('afinidad_info_filas')}</p>
  <p class="nota compacta">{i18n.t('afinidad_info_medio_punto')}</p>

  {#if cambios.length > 0}
    <hr class="separador" />
    <h3 class="titulo-cambios">{i18n.t('afinidad_info_cambios')}</h3>
    <p class="nota compacta">{i18n.t('afinidad_info_cambios_desc')}</p>
    <ul class="lista-cambios">
      {#each cambios as cambio (cambio.slot)}
        {@const delta = cambio.total - total}
        <li class="fila-cambio">
          <span class="cambio-texto">
            <span class="cambio-rol">{i18n.t(ETIQUETAS_ROL[cambio.slot])}</span>
            <span class="cambio-nombre">{cambio.nombre}</span>
          </span>
          <span class="cambio-total">Total {cambio.total}</span>
          <span
            class="cambio-delta"
            style="color:{delta > 0 ? RankGreat : delta < 0 ? RankFair : 'var(--texto-secundario)'}"
          >
            {delta > 0 ? '+' : ''}{delta}
          </span>
        </li>
      {/each}
    </ul>
  {/if}

  <button type="button" class="boton-texto ancho" onclick={onCerrar}>{i18n.t('cerrar')}</button>
</div>
