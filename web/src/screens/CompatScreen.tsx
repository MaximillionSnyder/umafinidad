/* Pantalla Herencia (compatibilidad). Porte de ui/compat/CompatScreen.kt:
   slots de la genealogía, buscador difuso, grilla de personajes y panel
   de resultado. */

import { useMemo, useState } from 'react'
import type { Character } from '../domain/models'
import { displayName } from '../domain/models'
import { coincideDifuso, rankearSugerencias } from '../domain/busqueda'
import { ModoGrilla } from '../data/prefs'
import { useAppStore, store } from '../state/store'
import { QuitarResultado, ToggleResultado } from '../state/resultado'
import { useI18n } from '../i18n'
import { colorDeAvatar, colorDeGenealogia } from '../theme/theme'
import { Avatar } from '../components/Avatar'
import { HeaderBar } from '../components/HeaderBar'
import { BottomSheet, Dialogo } from '../components/Modal'
import { useSnackbar } from '../components/snackbar'
import { ResultadoPanel } from '../components/ResultadoPanel'
import { IconBuscar, IconCerrar, IconCorazon } from '../components/Icons'

const ETIQUETAS_ROL = [
  'rol_hijo',
  'rol_padre1',
  'rol_padre2',
  'rol_abuelo1_p1',
  'rol_abuelo2_p1',
  'rol_abuelo1_p2',
  'rol_abuelo2_p2',
]

function etiquetaRol(i: number): string {
  return ETIQUETAS_ROL[i]
}

function rolCorto(i: number): string {
  if (i === 0) return 'rol_corto_hijo'
  if (i <= 2) return 'rol_corto_padre'
  return 'rol_corto_abuelo'
}

function posicionesRol(seleccion: (number | null)[], id: number): number[] {
  const out: number[] = []
  seleccion.forEach((v, i) => {
    if (v === id) out.push(i)
  })
  return out
}

export function CompatScreen() {
  const { modelo, seleccion, resultado, modoGrilla } = useAppStore()
  const { avisar } = useSnackbar()
  const { t, japones } = useI18n()
  const [filtro, setFiltro] = useState('')
  const [sheetAbierto, setSheetAbierto] = useState(false)
  const [dialogoQuitar, setDialogoQuitar] = useState(false)
  const [sugerenciaActiva, setSugerenciaActiva] = useState(-1)

  const sugerencias = useMemo(
    () => (modelo && filtro.trim().length >= 2 ? rankearSugerencias(modelo.personajes, filtro) : []),
    [modelo, filtro],
  )

  const seleccionSet = useMemo(() => new Set(seleccion.filter((v): v is number => v !== null)), [seleccion])

  const filtrados = useMemo(() => {
    if (!modelo) return []
    return modelo.personajes
      .filter((c) => c.playable === true && c.active === true)
      .filter((c) => coincideDifuso(c, filtro))
      .map((c, i) => ({ c, i }))
      .sort((a, b) => {
        const ra = seleccionSet.has(a.c.charId) ? 0 : 1
        const rb = seleccionSet.has(b.c.charId) ? 0 : 1
        return ra - rb || a.i - b.i
      })
      .map((x) => x.c)
  }, [modelo, filtro, seleccionSet])

  if (!modelo) {
    return <div className="centrado">{t('calculando')}</div>
  }

  const seleccionados = seleccion.filter((v) => v !== null).length

  function elegirSugerencia(id: number) {
    const r = store.toggle(id)
    if (r === ToggleResultado.COLOCADO || r === ToggleResultado.QUITADO) {
      setFiltro('')
      setSugerenciaActiva(-1)
    }
  }

  function manejarToggle(id: number) {
    const r = store.toggle(id)
    if (r === ToggleResultado.SELECCION_COMPLETA) avisar(t('seleccion_completa'))
    else if (r === ToggleResultado.REGLA) avisar(t('regla_slots'))
  }

  function manejarQuitar(i: number) {
    if (store.quitarSlot(i) === QuitarResultado.NECESITA_CONFIRMACION) {
      setDialogoQuitar(true)
    }
  }

  function onBuscarKeyDown(evento: React.KeyboardEvent<HTMLInputElement>) {
    if (sugerencias.length === 0) return
    if (evento.key === 'ArrowDown') {
      evento.preventDefault()
      setSugerenciaActiva((i) => Math.min(i + 1, sugerencias.length - 1))
    } else if (evento.key === 'ArrowUp') {
      evento.preventDefault()
      setSugerenciaActiva((i) => Math.max(i - 1, 0))
    } else if (evento.key === 'Enter' && sugerenciaActiva >= 0) {
      evento.preventDefault()
      elegirSugerencia(sugerencias[sugerenciaActiva].charId)
    } else if (evento.key === 'Escape') {
      setSugerenciaActiva(-1)
    }
  }

  return (
    <div className="pantalla compat">
      <HeaderBar
        titulo={t('seccion_herencia')}
        pillTexto={t('herencia_contador', seleccionados)}
      />

      {/* Slots de la genealogía, siempre visibles */}
      <div className="slots">
        <div className="slots-fila">
          {[0, 1, 2].map((i) => (
            <SlotChip key={i} slot={i} onQuitar={manejarQuitar} />
          ))}
        </div>
        <div className="slots-fila">
          {[3, 4, 5, 6].map((i) => (
            <SlotChip key={i} slot={i} onQuitar={manejarQuitar} />
          ))}
        </div>
      </div>

      {/* Buscador con autocompletado difuso */}
      <div className="buscador">
        <div className="buscador-campo">
          <IconBuscar className="buscador-icono" />
          <input
            type="search"
            role="combobox"
            aria-expanded={sugerencias.length > 0}
            aria-controls="sugerencias-lista"
            aria-autocomplete="list"
            placeholder={t('buscar')}
            value={filtro}
            onChange={(evento) => {
              setFiltro(evento.target.value)
              setSugerenciaActiva(-1)
            }}
            onKeyDown={onBuscarKeyDown}
          />
          {filtro !== '' ? (
            <button type="button" className="icon-button" onClick={() => setFiltro('')} aria-label={t('limpiar_todo')}>
              <IconCerrar />
            </button>
          ) : null}
        </div>

        {sugerencias.length > 0 ? (
          <ul id="sugerencias-lista" role="listbox" className="sugerencias">
            {sugerencias.map((c, indice) => {
              const principal = displayName(c, japones)
              const secundario = japones ? c.enName ?? '' : c.jpName ?? ''
              return (
                <li key={c.charId} role="option" aria-selected={indice === sugerenciaActiva}>
                  <button
                    type="button"
                    className={indice === sugerenciaActiva ? 'sugerencia activa' : 'sugerencia'}
                    onClick={() => elegirSugerencia(c.charId)}
                    onMouseEnter={() => setSugerenciaActiva(indice)}
                  >
                    <Avatar id={c.charId} nombre={principal} tamano={32} />
                    <span className="sugerencia-texto">
                      <span className="sugerencia-principal">{principal}</span>
                      {secundario !== '' ? <span className="sugerencia-secundaria">{secundario}</span> : null}
                    </span>
                  </button>
                </li>
              )
            })}
          </ul>
        ) : null}
      </div>

      {/* Grilla de personajes */}
      {filtrados.length === 0 ? (
        <div className="centrado">
          <div>
            <IconBuscar />
            <p>{t('sin_resultados')}</p>
          </div>
        </div>
      ) : modoGrilla === ModoGrilla.TARJETAS ? (
        <GrillaTarjetas
          personajes={filtrados}
          seleccion={seleccion}
          japones={japones}
          onToggle={manejarToggle}
        />
      ) : (
        <GrillaLista
          personajes={filtrados}
          seleccion={seleccion}
          japones={japones}
          onToggle={manejarToggle}
        />
      )}

      {seleccionados > 0 ? (
        <button type="button" className="fab" aria-live="polite" onClick={() => setSheetAbierto(true)}>
          <IconCorazon />
          {t('ver_afinidad')}
        </button>
      ) : null}

      <BottomSheet open={sheetAbierto && resultado !== null} onClose={() => setSheetAbierto(false)} labelledBy="resultado-titulo">
        {resultado ? <ResultadoPanel modelo={modelo} res={resultado} japones={japones} /> : null}
      </BottomSheet>

      <Dialogo
        open={dialogoQuitar}
        titulo={t('quitar_hijo_titulo')}
        onClose={() => setDialogoQuitar(false)}
      >
        <button
          type="button"
          className="boton-texto"
          onClick={() => {
            store.confirmarQuitarSoloHijo()
            setDialogoQuitar(false)
          }}
        >
          {t('quitar_solo_hijo')}
        </button>
        <button
          type="button"
          className="boton-texto"
          onClick={() => {
            store.limpiarTodo()
            setDialogoQuitar(false)
          }}
        >
          {t('limpiar_todo')}
        </button>
      </Dialogo>
    </div>
  )
}

/* ---------- Slots coloreados por genealogía ---------- */

function SlotChip({ slot, onQuitar }: { slot: number; onQuitar: (slot: number) => void }) {
  const { modelo, seleccion } = useAppStore()
  const { t, japones } = useI18n()
  const id = seleccion[slot]
  const personaje = id !== null && modelo ? modelo.porId(id) : null
  const rolColor = colorDeGenealogia(slot)
  const borde = personaje ? `color-mix(in srgb, ${rolColor} 70%, transparent)` : 'var(--contorno-variante)'

  if (!personaje) {
    return (
      <div className="slot-chip vacio" style={{ borderColor: borde }}>
        <span className="slot-etiqueta" style={{ color: rolColor }}>
          {t(etiquetaRol(slot))}
        </span>
        <span className="slot-nombre">—</span>
      </div>
    )
  }

  return (
    <button
      type="button"
      className="slot-chip"
      style={{ borderColor: borde }}
      onClick={() => onQuitar(slot)}
      aria-label={`${t(etiquetaRol(slot))}: ${displayName(personaje, japones)}. ${t('quitar_personaje')}`}
    >
      <span className="slot-etiqueta" style={{ color: rolColor }}>
        {t(etiquetaRol(slot))}
      </span>
      <span className="slot-nombre seleccionado">{displayName(personaje, japones)}</span>
    </button>
  )
}

/* ---------- Modo TARJETAS ---------- */

function GrillaTarjetas({
  personajes,
  seleccion,
  japones,
  onToggle,
}: {
  personajes: Character[]
  seleccion: (number | null)[]
  japones: boolean
  onToggle: (id: number) => void
}) {
  return (
    <ul className="grilla-tarjetas">
      {personajes.map((c) => (
        <li key={c.charId}>
          <CardTarjeta personaje={c} seleccion={seleccion} japones={japones} onToggle={onToggle} />
        </li>
      ))}
    </ul>
  )
}

function CardTarjeta({
  personaje,
  seleccion,
  japones,
  onToggle,
}: {
  personaje: Character
  seleccion: (number | null)[]
  japones: boolean
  onToggle: (id: number) => void
}) {
  const { t } = useI18n()
  const principal = displayName(personaje, japones)
  const secundario = japones ? personaje.enName ?? '' : personaje.jpName ?? ''
  const roles = posicionesRol(seleccion, personaje.charId)
  const seleccionado = roles.length > 0
  const rolesTexto = roles.map((r) => t(rolCorto(r))).join(', ')

  return (
    <button
      type="button"
      role="checkbox"
      aria-checked={seleccionado}
      aria-describedby={rolesTexto !== '' ? `roles-${personaje.charId}` : undefined}
      className={seleccionado ? 'card-tarjeta seleccionada' : 'card-tarjeta'}
      onClick={() => onToggle(personaje.charId)}
    >
      {rolesTexto !== '' ? (
        <span id={`roles-${personaje.charId}`} className="oculto-visualmente">
          {rolesTexto}
        </span>
      ) : null}
      <span className="avatar-envoltura">
        <Avatar id={personaje.charId} nombre={principal} tamano={64} />
        {seleccionado ? (
          <span className="check" aria-hidden="true">
            ✓
          </span>
        ) : null}
      </span>
      <span className="nombre-principal">{principal}</span>
      {secundario !== '' ? <span className="nombre-secundario">{secundario}</span> : null}
      {roles.length > 0 ? (
        <span className="roles-flow">
          {roles.map((r) => (
            <span key={r} className="rol-tag">
              {t(rolCorto(r))}
            </span>
          ))}
        </span>
      ) : null}
    </button>
  )
}

/* ---------- Modo LISTA ---------- */

function GrillaLista({
  personajes,
  seleccion,
  japones,
  onToggle,
}: {
  personajes: Character[]
  seleccion: (number | null)[]
  japones: boolean
  onToggle: (id: number) => void
}) {
  return (
    <ul className="grilla-lista">
      {personajes.map((c) => (
        <li key={c.charId}>
          <CardFila personaje={c} seleccion={seleccion} japones={japones} onToggle={onToggle} />
        </li>
      ))}
    </ul>
  )
}

function CardFila({
  personaje,
  seleccion,
  japones,
  onToggle,
}: {
  personaje: Character
  seleccion: (number | null)[]
  japones: boolean
  onToggle: (id: number) => void
}) {
  const { t } = useI18n()
  const principal = displayName(personaje, japones)
  const secundario = japones ? personaje.enName ?? '' : personaje.jpName ?? ''
  const roles = posicionesRol(seleccion, personaje.charId)
  const seleccionado = roles.length > 0
  const rolesTexto = roles.map((r) => t(rolCorto(r))).join(', ')

  return (
    <button
      type="button"
      role="checkbox"
      aria-checked={seleccionado}
      aria-describedby={rolesTexto !== '' ? `roles-${personaje.charId}` : undefined}
      className={seleccionado ? 'card-fila seleccionada' : 'card-fila'}
      onClick={() => onToggle(personaje.charId)}
    >
      {rolesTexto !== '' ? (
        <span id={`roles-${personaje.charId}`} className="oculto-visualmente">
          {rolesTexto}
        </span>
      ) : null}
      <span className="franja" style={{ background: colorDeAvatar(personaje.charId) }} aria-hidden="true" />
      <Avatar id={personaje.charId} nombre={principal} tamano={48} />
      <span className="card-fila-texto">
        <span className="nombre-principal">{principal}</span>
        {secundario !== '' ? <span className="nombre-secundario">{secundario}</span> : null}
      </span>
      {seleccionado ? (
        <span className="roles-flow">
          {roles.map((r) => (
            <span key={r} className="rol-tag">
              {t(rolCorto(r))}
            </span>
          ))}
          <span className="check-texto" aria-hidden="true">
            ✓
          </span>
        </span>
      ) : null}
    </button>
  )
}
