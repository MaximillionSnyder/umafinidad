<script lang="ts">
/* Detalle de "Mi corredora": mejor linaje exacto por hija + edición por
   alternativas de slot + configuraciones guardadas. */

import { goto } from '$app/navigation'
import { resolve } from '$app/paths'
import type { AffinityModel, Linaje } from '$lib/domain/affinity'
import type { ArbolGuardado } from '$lib/data/arboles'
import type { CodigoIdioma } from '$lib/i18n'
import type { Seleccion } from '$lib/domain/herencia'
import { displayName } from '$lib/domain/models'
import { store } from '$lib/state/store.svelte'
import { calcularResultado } from '$lib/state/resultado'
import { crearI18n } from '$lib/i18n'
import { Async } from '$lib/state/async.svelte'
import { pedirMejorLinaje } from '$lib/worker/cliente'
import Avatar from '$lib/components/Avatar.svelte'
import RankPill from '$lib/components/RankPill.svelte'
import ResultadoPanel from '$lib/components/ResultadoPanel.svelte'
import BottomSheet from '$lib/components/BottomSheet.svelte'
import Dialogo from '$lib/components/Dialogo.svelte'
import { useSnackbar } from '$lib/components/snackbar'
import Icono from '$lib/components/Icono.svelte'
import HojaAlternativas from './HojaAlternativas.svelte'
import ChipRol from './ChipRol.svelte'

const ETIQUETAS_ROL = [
  'rol_hijo',
  'rol_padre1',
  'rol_padre2',
  'rol_abuelo1_p1',
  'rol_abuelo2_p1',
  'rol_abuelo1_p2',
  'rol_abuelo2_p2',
]

function listaDeLinaje(l: Linaje): Seleccion {
  return [
    l.hijo.charId,
    l.padre.charId,
    l.madre.charId,
    l.abuelos[0][0].charId,
    l.abuelos[0][1].charId,
    l.abuelos[1][0].charId,
    l.abuelos[1][1].charId,
  ]
}

interface Props {
  modelo: AffinityModel
  hijoId: number
  override: Seleccion | null
  onRecargarCon: (seleccion: Seleccion) => void
  onConteo: (conteo: number) => void
}

let { modelo, hijoId, override, onRecargarCon, onConteo }: Props = $props()
const i18n = $derived(crearI18n(store.idioma))
const { avisar } = useSnackbar()

let seleccionActual = $state.raw<Seleccion>([])
let seleccionOptima = $state.raw<Seleccion>([])
let sheetSlot = $state<number | null>(null)
let mostrarGuardar = $state(false)
let textoNombre = $state('')
let linajeAplicado = $state.raw<Linaje | null>(null)

const linaje = new Async(() => pedirMejorLinaje(hijoId))

$effect(() => {
  void hijoId
  void override
  void linaje.ejecutar()
})

$effect(() => {
  const l = linaje.datos
  if (l === null || l === linajeAplicado) return
  linajeAplicado = l
  const optima = listaDeLinaje(l)
  seleccionOptima = optima
  seleccionActual = override !== null && override.length === 7 ? [...override] : optima
})

$effect(() => {
  onConteo(seleccionActual.filter((v) => v !== null).length)
})

const nombreHijo = $derived(displayName(modelo.porId(hijoId)!, i18n.japones))
const res = $derived(seleccionActual.length === 7 ? calcularResultado(modelo, seleccionActual) : null)
const totalActual = $derived(res?.total ?? 0)
const esOptimo = $derived(
  seleccionActual.length === 7 &&
    seleccionOptima.length === 7 &&
    seleccionActual.every((v, i) => v === seleccionOptima[i]),
)
const guardadas = $derived(store.arboles.filter((a) => a.hijoId === hijoId))
const nombreSugerido = $derived(i18n.t('nombre_sugerido', nombreHijo, totalActual))

function elegirAlternativa(nuevoId: number): void {
  if (sheetSlot === null) return
  const copia = [...seleccionActual]
  copia[sheetSlot] = nuevoId
  seleccionActual = copia
  sheetSlot = null
}
</script>

{#if linaje.cargando}
  <div class="centrado">{i18n.t('calculando')}</div>
{:else if linaje.datos === null || seleccionActual.length !== 7}
  <p class="nota">{i18n.t('corredora_invalida')}</p>
{:else}
  <div class="detalle-corredora">
    <section class="card panel-mejor">
      <div class="panel-mejor-cabecera">
        <Avatar id={hijoId} nombre={nombreHijo} tamano={48} />
        <h2>{i18n.t('mejor_de', nombreHijo)}</h2>
        <RankPill rango={modelo.rangoTotal(totalActual)} puntos={totalActual} grande />
      </div>

      <hr class="separador" />

      {#each [0, 1, 2, 3, 4, 5, 6] as slot (slot)}
        <ChipRol
          {slot}
          etiqueta={i18n.t(ETIQUETAS_ROL[slot])}
          id={seleccionActual[slot]}
          onAbrir={slot === 0 ? undefined : () => (sheetSlot = slot)}
        />
      {/each}

      <button
        type="button"
        class="boton-primario"
        onclick={() => {
          store.cargarSeleccion(seleccionActual)
          void goto(resolve('/compat'))
        }}
      >
        {i18n.t('ver_herencia')}
      </button>
    </section>

    {#if !esOptimo}
      <button
        type="button"
        class="boton-texto ancho"
        onclick={() => (seleccionActual = [...seleccionOptima])}
      >
        {i18n.t('restablecer_optimo')}
      </button>
    {/if}

    {#if res !== null}
      <ResultadoPanel {modelo} res={res} japones={i18n.japones} />
    {/if}

    <button type="button" class="boton-primario ancho" onclick={() => (mostrarGuardar = true)}>
      {i18n.t('guardar_config')}
    </button>

    {#if guardadas.length > 0}
      <h3 class="titulo-seccion">{i18n.t('guardadas_de', nombreHijo)}</h3>
      <ul class="lista-guardadas">
        {#each guardadas as a (a.id)}
          {@render tarjetaGuardada(a, i18n.codigo)}
        {/each}
      </ul>
    {/if}

    <Dialogo
      open={mostrarGuardar}
      titulo={i18n.t('guardar_config')}
      onClose={() => (mostrarGuardar = false)}
    >
      <input
        type="text"
        class="campo-texto"
        placeholder={nombreSugerido}
        bind:value={textoNombre}
        aria-label={i18n.t('guardar_config')}
      />
      <button
        type="button"
        class="boton-texto"
        onclick={() => {
          store.guardarArbol(
            hijoId,
            textoNombre.trim() === '' ? nombreSugerido : textoNombre,
            seleccionActual,
            totalActual,
          )
          mostrarGuardar = false
          textoNombre = ''
          avisar(i18n.t('guardado_snack'))
        }}
      >
        {i18n.t('guardar')}
      </button>
      <button type="button" class="boton-texto" onclick={() => (mostrarGuardar = false)}>
        {i18n.t('cancelar')}
      </button>
    </Dialogo>

    <BottomSheet
      open={sheetSlot !== null}
      onClose={() => (sheetSlot = null)}
      labelledBy="alternativas-titulo"
    >
      {#if sheetSlot !== null}
        {#key sheetSlot}
          <HojaAlternativas
            seleccion={seleccionActual}
            slot={sheetSlot}
            ocupanteEtiqueta={i18n.t(ETIQUETAS_ROL[sheetSlot])}
            onElegir={elegirAlternativa}
          />
        {/key}
      {/if}
    </BottomSheet>
  </div>
{/if}

{#snippet tarjetaGuardada(guardada: ArbolGuardado, codigo: CodigoIdioma)}
  {@const fecha = new Intl.DateTimeFormat(codigo, {
    day: '2-digit',
    month: '2-digit',
    year: '2-digit',
  }).format(new Date(guardada.creadoEn))}
  <li>
    <article class="card tarjeta-guardada">
      <button
        type="button"
        class="tarjeta-guardada-abrir"
        onclick={() => onRecargarCon(guardada.seleccion)}
      >
        <span class="tarjeta-guardada-nombre">{guardada.nombre}</span>
        <span class="tarjeta-guardada-meta">◎ {guardada.total} · {fecha}</span>
      </button>
      <button
        type="button"
        class="icon-button"
        onclick={() => {
          store.eliminarArbol(guardada.id)
          avisar(i18n.t('eliminado_snack'))
        }}
        aria-label={i18n.t('cancelar')}
      >
        <Icono nombre="cerrar" />
      </button>
    </article>
  </li>
{/snippet}
