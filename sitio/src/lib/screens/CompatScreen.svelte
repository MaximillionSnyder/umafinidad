<script lang="ts">
/* Herencia (compatibilidad): slots de la genealogía, buscador difuso,
   grilla de personajes y panel de resultado. */

import type { Character } from '$lib/domain/models'
import { displayName } from '$lib/domain/models'
import { puedeIrEn, SlotEstado } from '$lib/domain/herencia'
import { coincideDifuso, rankearSugerencias } from '$lib/domain/busqueda'
import { ModoGrilla } from '$lib/data/prefs'
import { store } from '$lib/state/store.svelte'
import { AgregarResultado, QuitarResultado, ToggleResultado } from '$lib/state/resultado'
import { crearI18n } from '$lib/i18n'
import { enlaceSeleccion, PARAM_SELECCION } from '$lib/state/compartir'
import { page } from '$app/state'
import { colorDeAvatar, colorDeGenealogia } from '$lib/theme/theme'
import Avatar from '$lib/components/Avatar.svelte'
import HeaderBar from '$lib/components/HeaderBar.svelte'
import BottomSheet from '$lib/components/BottomSheet.svelte'
import Dialogo from '$lib/components/Dialogo.svelte'
import { useSnackbar } from '$lib/components/snackbar'
import ResultadoPanel from '$lib/components/ResultadoPanel.svelte'
import Icono from '$lib/components/Icono.svelte'

const ETIQUETAS_ROL = [
  'rol_hijo',
  'rol_padre1',
  'rol_padre2',
  'rol_abuelo1_p1',
  'rol_abuelo2_p1',
  'rol_abuelo1_p2',
  'rol_abuelo2_p2',
]

const i18n = $derived(crearI18n(store.idioma))
const { avisar } = useSnackbar()

let filtro = $state('')
let sheetAbierto = $state(false)
let dialogoQuitar = $state(false)
let sugerenciaActiva = $state(-1)
/* Personaje al que se le está eligiendo lugar; null = hoja cerrada. */
let idElegir = $state<number | null>(null)

/* Con destino marcado solo se sugieren personajes que las reglas dejan poner
   ahí (igual que en el panel de la burbuja). */
const candidatos = $derived.by(() => {
  if (!store.modelo) return []
  const destino = store.slotDestino
  if (destino === null) return store.modelo.personajes
  return store.modelo.personajes.filter((c) =>
    puedeIrEn(store.seleccion, destino, c.charId),
  )
})
const sugerencias = $derived(
  store.modelo && filtro.trim().length >= 2 ? rankearSugerencias(candidatos, filtro) : [],
)
const seleccionSet = $derived(new Set(store.seleccion.filter((v): v is number => v !== null)))
const filtrados = $derived.by(() => {
  if (!store.modelo) return []
  return store.modelo.personajes
    .filter((c) => c.playable === true && c.active === true)
    .filter((c) => coincideDifuso(c, filtro))
    .map((c, i) => ({ c, i }))
    .sort((a, b) => {
      const ra = seleccionSet.has(a.c.charId) ? 0 : 1
      const rb = seleccionSet.has(b.c.charId) ? 0 : 1
      return ra - rb || a.i - b.i
    })
    .map((x) => x.c)
})
const seleccionados = $derived(store.seleccion.filter((v) => v !== null).length)

/* Selección recibida por URL (?s=...): se aplica una sola vez, cuando el
   modelo ya está listo para validar los ids. */
let compartidoAplicado = $state(false)
$effect(() => {
  if (store.modelo === null || compartidoAplicado) return
  compartidoAplicado = true
  const raw = page.url.searchParams.get(PARAM_SELECCION)
  if (raw !== null && !store.aplicarSeleccionCompartida(raw)) {
    avisar(i18n.t('enlace_invalido'))
  }
})

async function compartir(): Promise<void> {
  if (seleccionados === 0) return
  const url = enlaceSeleccion(window.location.pathname, store.seleccion)
  if (typeof navigator.share === 'function') {
    try {
      await navigator.share({ title: 'Uma Afinidad', url })
    } catch {
      /* el usuario canceló la hoja de compartir */
    }
    return
  }
  try {
    await navigator.clipboard.writeText(url)
    avisar(i18n.t('enlace_copiado'))
  } catch {
    avisar(i18n.t('enlace_invalido'))
  }
}

function posicionesRol(id: number): number[] {
  const out: number[] = []
  store.seleccion.forEach((v, i) => {
    if (v === id) out.push(i)
  })
  return out
}

function rolCorto(i: number): string {
  if (i === 0) return 'rol_corto_hijo'
  if (i <= 2) return 'rol_corto_padre'
  return 'rol_corto_abuelo'
}

/* Elegir un personaje ya colocado abre el selector de lugar, para ponerlo
   también en otro slot (el hijo puede ser abuelo) o para quitar una de sus
   copias. Solo si hay algún slot libre donde las reglas lo dejan; si no, se
   quita como antes. Con un destino marcado manda el destino. Devuelve true
   si la genealogía llegó a cambiar. */
function manejarToggle(id: number): boolean {
  const yaElegido = store.seleccion.includes(id)
  if (yaElegido && store.slotDestino === null) {
    if (store.haySlotValido(id)) {
      idElegir = id
      return false
    }
    quitarUltima(id)
    return true
  }
  const r = store.toggle(id)
  if (r === ToggleResultado.SELECCION_COMPLETA) avisar(i18n.t('seleccion_completa'))
  else if (r === ToggleResultado.REGLA) avisar(i18n.t('regla_slots'))
  return r === ToggleResultado.COLOCADO || r === ToggleResultado.QUITADO
}

function elegirSugerencia(id: number): void {
  if (!manejarToggle(id)) return
  filtro = ''
  sugerenciaActiva = -1
}

function tocarSlot(i: number): void {
  if (store.seleccion[i] !== null) manejarQuitar(i)
  else store.marcarDestino(i)
}

function manejarQuitar(i: number): void {
  if (store.quitarSlot(i) === QuitarResultado.NECESITA_CONFIRMACION) {
    dialogoQuitar = true
  }
}

/* Quita la última copia del personaje, pasando por la confirmación si es el
   hijo (igual que tocar su chip en la genealogía). */
function quitarUltima(id: number): void {
  for (let i = store.seleccion.length - 1; i >= 0; i--) {
    if (store.seleccion[i] === id) {
      manejarQuitar(i)
      return
    }
  }
}

/* Toca un slot del selector: si el personaje ya lo ocupa lo quita (con la
   confirmación del hijo), si está libre y las reglas lo dejan lo agrega como
   segunda copia. */
function elegirSlot(slot: number, estado: SlotEstado): void {
  const id = idElegir
  if (id === null) return
  if (estado === SlotEstado.ACTUAL) {
    quitarUltima(id)
    idElegir = null
  } else if (store.agregar(id, slot) === AgregarResultado.AGREGADO) {
    idElegir = null
  } else {
    avisar(i18n.t('regla_slots'))
  }
}

function onBuscarKeyDown(evento: KeyboardEvent): void {
  if (sugerencias.length === 0) return
  if (evento.key === 'ArrowDown') {
    evento.preventDefault()
    sugerenciaActiva = Math.min(sugerenciaActiva + 1, sugerencias.length - 1)
  } else if (evento.key === 'ArrowUp') {
    evento.preventDefault()
    sugerenciaActiva = Math.max(sugerenciaActiva - 1, 0)
  } else if (evento.key === 'Enter' && sugerenciaActiva >= 0) {
    evento.preventDefault()
    elegirSugerencia(sugerencias[sugerenciaActiva].charId)
  } else if (evento.key === 'Escape') {
    sugerenciaActiva = -1
  }
}
</script>

<svelte:head>
  <title>{i18n.t('seccion_herencia')} · Uma Afinidad</title>
</svelte:head>

{#if store.modelo === null}
  <div class="centrado">{i18n.t('calculando')}</div>
{:else}
  <div class="pantalla compat">
    <HeaderBar
      titulo={i18n.t('seccion_herencia')}
      pillTexto={i18n.t('herencia_contador', seleccionados)}
    />

    <div class="slots">
      <div class="slots-fila">
        {#each [0, 1, 2] as slot (slot)}
          {@render slotChip(slot)}
        {/each}
      </div>
      <div class="slots-fila">
        {#each [3, 4, 5, 6] as slot (slot)}
          {@render slotChip(slot)}
        {/each}
      </div>
      <!-- Con destino marcado se dice a qué rol va el próximo personaje
           (mismo texto que el panel de la burbuja). -->
      {#if store.slotDestino !== null}
        <p class="slots-destino" style="color:{colorDeGenealogia(store.slotDestino)}">
          {i18n.t('burbuja_destino', i18n.t(ETIQUETAS_ROL[store.slotDestino]))}
        </p>
      {/if}
    </div>

    <div class="buscador">
      <div class="buscador-campo">
        <Icono nombre="buscar" class="buscador-icono" />
        <input
          type="search"
          role="combobox"
          aria-expanded={sugerencias.length > 0}
          aria-controls="sugerencias-lista"
          aria-autocomplete="list"
          placeholder={i18n.t('buscar')}
          bind:value={filtro}
          oninput={() => (sugerenciaActiva = -1)}
          onkeydown={onBuscarKeyDown}
        />
        {#if filtro !== ''}
          <button
            type="button"
            class="icon-button"
            onclick={() => (filtro = '')}
            aria-label={i18n.t('limpiar_todo')}
          >
            <Icono nombre="cerrar" />
          </button>
        {/if}
      </div>

      {#if sugerencias.length > 0}
        <ul id="sugerencias-lista" role="listbox" class="sugerencias">
          {#each sugerencias as c, indice (c.charId)}
            {@const principal = displayName(c, i18n.japones)}
            {@const secundario = i18n.japones ? (c.enName ?? '') : (c.jpName ?? '')}
            <li role="option" aria-selected={indice === sugerenciaActiva}>
              <button
                type="button"
                class={indice === sugerenciaActiva ? 'sugerencia activa' : 'sugerencia'}
                onclick={() => elegirSugerencia(c.charId)}
                onmouseenter={() => (sugerenciaActiva = indice)}
              >
                <Avatar id={c.charId} nombre={principal} tamano={32} />
                <span class="sugerencia-texto">
                  <span class="sugerencia-principal">{principal}</span>
                  {#if secundario !== ''}
                    <span class="sugerencia-secundaria">{secundario}</span>
                  {/if}
                </span>
              </button>
            </li>
          {/each}
        </ul>
      {/if}
    </div>

    {#if filtrados.length === 0}
      <div class="centrado">
        <div>
          <Icono nombre="buscar" />
          <p>{i18n.t('sin_resultados')}</p>
        </div>
      </div>
    {:else if store.modoGrilla === ModoGrilla.TARJETAS}
      <ul class="grilla-tarjetas">
        {#each filtrados as c (c.charId)}
          {@render tarjeta(c)}
        {/each}
      </ul>
    {:else}
      <ul class="grilla-lista">
        {#each filtrados as c (c.charId)}
          {@render fila(c)}
        {/each}
      </ul>
    {/if}

    {#if seleccionados > 0}
      <button type="button" class="fab secundario" onclick={() => void compartir()}>
        {i18n.t('compartir')}
      </button>
      <button type="button" class="fab" aria-live="polite" onclick={() => (sheetAbierto = true)}>
        <Icono nombre="corazon" />
        {i18n.t('ver_afinidad')}
      </button>
    {/if}

    <BottomSheet
      open={sheetAbierto && store.resultado !== null}
      onClose={() => (sheetAbierto = false)}
      labelledBy="resultado-titulo"
    >
      {#if store.resultado !== null}
        <ResultadoPanel modelo={store.modelo} res={store.resultado} japones={i18n.japones} />
      {/if}
    </BottomSheet>

    <!-- Elegir de nuevo un personaje ya colocado: hoja con los 7 slots. -->
    <BottomSheet
      open={idElegir !== null}
      onClose={() => (idElegir = null)}
      labelledBy="elegir-slot-titulo"
    >
      {@const personajeElegir = idElegir !== null && store.modelo ? store.modelo.porId(idElegir) : null}
      {#if personajeElegir !== null}
        <div class="slot-picker">
          <h2 class="slot-picker-titulo" id="elegir-slot-titulo">
            {i18n.t('elegir_slot_titulo', displayName(personajeElegir, i18n.japones))}
          </h2>
          <ul class="slot-picker-lista">
            {#each store.slotsPara(idElegir!) as opcion (opcion.slot)}
              {@render filaSlot(opcion.slot, opcion.estado)}
            {/each}
          </ul>
        </div>
      {/if}
    </BottomSheet>

    <Dialogo
      open={dialogoQuitar}
      titulo={i18n.t('quitar_hijo_titulo')}
      onClose={() => (dialogoQuitar = false)}
    >
      <button
        type="button"
        class="boton-texto"
        onclick={() => {
          store.confirmarQuitarSoloHijo()
          dialogoQuitar = false
        }}
      >
        {i18n.t('quitar_solo_hijo')}
      </button>
      <button
        type="button"
        class="boton-texto"
        onclick={() => {
          store.limpiarTodo()
          dialogoQuitar = false
        }}
      >
        {i18n.t('limpiar_todo')}
      </button>
    </Dialogo>
  </div>
{/if}

{#snippet slotChip(slot: number)}
  {@const id = store.seleccion[slot]}
  {@const personaje = id !== null && store.modelo ? store.modelo.porId(id) : null}
  {@const rolColor = colorDeGenealogia(slot)}
  {@const destino = store.slotDestino === slot}
  {@const borde = destino
    ? 'var(--primario)'
    : personaje
      ? `color-mix(in srgb, ${rolColor} 70%, transparent)`
      : 'var(--contorno-variante)'}
  <!-- Ocupado = quitar; libre = marcar como destino de la próxima
       colocación (mismo gesto que el panel de la burbuja). -->
  <button
    type="button"
    class={destino ? 'slot-chip destino' : personaje ? 'slot-chip' : 'slot-chip vacio'}
    style="border-color:{borde}"
    aria-pressed={destino}
    onclick={() => tocarSlot(slot)}
    aria-label={personaje
      ? `${i18n.t(ETIQUETAS_ROL[slot])}: ${displayName(personaje, i18n.japones)}. ${i18n.t(
          'quitar_personaje',
        )}`
      : `${i18n.t(ETIQUETAS_ROL[slot])}. ${i18n.t('burbuja_elegir_lugar')}`}
  >
    <span class="slot-etiqueta" style="color:{rolColor}">{i18n.t(ETIQUETAS_ROL[slot])}</span>
    <span class="slot-nombre">{personaje ? displayName(personaje, i18n.japones) : '—'}</span>
  </button>
{/snippet}

{#snippet filaSlot(slot: number, estado: SlotEstado)}
  {@const rolColor = colorDeGenealogia(slot)}
  {@const id = store.seleccion[slot]}
  {@const ocupante = id !== null && store.modelo ? store.modelo.porId(id) : null}
  {@const detalle =
    estado === SlotEstado.ACTUAL
      ? i18n.t('slot_aqui')
      : estado === SlotEstado.BLOQUEADO
        ? i18n.t('slot_bloqueado')
        : ocupante
          ? displayName(ocupante, i18n.japones)
          : '—'}
  <!-- Solo se pueden tocar los slots donde el personaje se agrega o se quita;
       los que tienen a otro o que las reglas vetan se anuncian igual. -->
  {@const accionable = estado === SlotEstado.ACTUAL || estado === SlotEstado.VALIDO}
  <li>
    <button
      type="button"
      class={accionable ? 'slot-fila' : 'slot-fila bloqueada'}
      style="border-color:{accionable ? `color-mix(in srgb, ${rolColor} 70%, transparent)` : undefined}"
      disabled={!accionable}
      onclick={() => elegirSlot(slot, estado)}
      aria-label={`${i18n.t(ETIQUETAS_ROL[slot])}. ${detalle}. ${
        estado === SlotEstado.ACTUAL
          ? i18n.t('slot_quitar_aca')
          : estado === SlotEstado.VALIDO
            ? i18n.t('slot_agregar')
            : ''
      }`}
    >
      <span class="slot-etiqueta" style="color:{rolColor}">{i18n.t(ETIQUETAS_ROL[slot])}</span>
      <span class="slot-fila-detalle">{detalle}</span>
      {#if estado === SlotEstado.ACTUAL}<span class="check-texto" aria-hidden="true">✓</span>{/if}
    </button>
  </li>
{/snippet}

{#snippet tarjeta(personaje: Character)}
  {@const principal = displayName(personaje, i18n.japones)}
  {@const secundario = i18n.japones ? (personaje.enName ?? '') : (personaje.jpName ?? '')}
  {@const roles = posicionesRol(personaje.charId)}
  {@const seleccionado = roles.length > 0}
  {@const rolesTexto = roles.map((r) => i18n.t(rolCorto(r))).join(', ')}
  <li>
    <button
      type="button"
      role="checkbox"
      aria-checked={seleccionado}
      aria-describedby={rolesTexto !== '' ? `roles-${personaje.charId}` : undefined}
      class={seleccionado ? 'card-tarjeta seleccionada' : 'card-tarjeta'}
      onclick={() => manejarToggle(personaje.charId)}
    >
      {#if rolesTexto !== ''}
        <span id={`roles-${personaje.charId}`} class="oculto-visualmente">{rolesTexto}</span>
      {/if}
      <span class="avatar-envoltura">
        <Avatar id={personaje.charId} nombre={principal} tamano={64} />
        {#if seleccionado}
          <span class="check" aria-hidden="true">✓</span>
        {/if}
      </span>
      <span class="nombre-principal">{principal}</span>
      {#if secundario !== ''}
        <span class="nombre-secundario">{secundario}</span>
      {/if}
      {#if roles.length > 0}
        <span class="roles-flow">
          {#each roles as r (r)}
            <span class="rol-tag">{i18n.t(rolCorto(r))}</span>
          {/each}
        </span>
      {/if}
    </button>
  </li>
{/snippet}

{#snippet fila(personaje: Character)}
  {@const principal = displayName(personaje, i18n.japones)}
  {@const secundario = i18n.japones ? (personaje.enName ?? '') : (personaje.jpName ?? '')}
  {@const roles = posicionesRol(personaje.charId)}
  {@const seleccionado = roles.length > 0}
  {@const rolesTexto = roles.map((r) => i18n.t(rolCorto(r))).join(', ')}
  <li>
    <button
      type="button"
      role="checkbox"
      aria-checked={seleccionado}
      aria-describedby={rolesTexto !== '' ? `roles-${personaje.charId}` : undefined}
      class={seleccionado ? 'card-fila seleccionada' : 'card-fila'}
      onclick={() => manejarToggle(personaje.charId)}
    >
      {#if rolesTexto !== ''}
        <span id={`roles-${personaje.charId}`} class="oculto-visualmente">{rolesTexto}</span>
      {/if}
      <span
        class="franja"
        style="background:{colorDeAvatar(personaje.charId)}"
        aria-hidden="true"
      ></span>
      <Avatar id={personaje.charId} nombre={principal} tamano={48} />
      <span class="card-fila-texto">
        <span class="nombre-principal">{principal}</span>
        {#if secundario !== ''}
          <span class="nombre-secundario">{secundario}</span>
        {/if}
      </span>
      {#if seleccionado}
        <span class="roles-flow">
          {#each roles as r (r)}
            <span class="rol-tag">{i18n.t(rolCorto(r))}</span>
          {/each}
          <span class="check-texto" aria-hidden="true">✓</span>
        </span>
      {/if}
    </button>
  </li>
{/snippet}
