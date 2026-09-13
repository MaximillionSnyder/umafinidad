/* Mi corredora: mejor linaje exacto por hija + edición por alternativas de
   slot + configuraciones guardadas. Porte de ui/corredora/CorredoraScreen.kt. */

import { useEffect, useMemo, useState } from 'react'
import type { AffinityModel, AlternativaSlot, Linaje } from '../domain/affinity'
import type { ArbolGuardado } from '../data/arboles'
import type { Seleccion } from '../domain/herencia'
import { displayName } from '../domain/models'
import { rankearSugerencias } from '../domain/busqueda'
import { calcularResultado } from '../state/resultado'
import { store, useAppStore } from '../state/store'
import { useI18n, type CodigoIdioma } from '../i18n'
import { irA } from '../state/navegacion'
import { pedirAlternativas, pedirMejorLinaje } from '../worker/cliente'
import { useAsync } from '../state/useAsync'
import { Avatar } from '../components/Avatar'
import { HeaderBar } from '../components/HeaderBar'
import { RankPill } from '../components/RankPill'
import { ResultadoPanel } from '../components/ResultadoPanel'
import { BottomSheet, Dialogo } from '../components/Modal'
import { useSnackbar } from '../components/snackbar'
import { IconBuscar, IconCerrar } from '../components/Icons'
import { colorDeGenealogia, colorDeRango } from '../theme/theme'

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

export function CorredoraScreen() {
  const { modelo, arbolPendiente } = useAppStore()
  const { t, japones } = useI18n()
  const [filtro, setFiltro] = useState('')
  const [elegidaId, setElegidaId] = useState(-1)
  const [recarga, setRecarga] = useState(0)
  const [conteoSeleccion, setConteoSeleccion] = useState(0)
  const [override, setOverride] = useState<Seleccion | null>(null)
  const [pendienteProcesado, setPendienteProcesado] = useState<ArbolGuardado | null>(null)

  /* Config pedida desde Ajustes o desde la lista: ajuste en render
     (patrón recomendado por React para sincronizar props externas). */
  if (arbolPendiente !== null && arbolPendiente !== pendienteProcesado) {
    setPendienteProcesado(arbolPendiente)
    setElegidaId(arbolPendiente.hijoId)
    setFiltro('')
    setOverride(arbolPendiente.seleccion)
    setRecarga((r) => r + 1)
  }

  useEffect(() => {
    if (arbolPendiente !== null) store.consumirArbolPendiente()
  }, [arbolPendiente])

  if (!modelo) return <div className="centrado">{t('calculando')}</div>

  const sugerencias =
    filtro.trim().length >= 2 ? rankearSugerencias(modelo.personajes, filtro) : []

  const elegida = elegidaId > 0 ? modelo.porId(elegidaId) : null

  return (
    <div className="pantalla">
      <HeaderBar
        titulo={t('tab_corredora')}
        pillTexto={t('herencia_contador', conteoSeleccion)}
        chip={
          elegida ? (
            <span className="chip-nino">
              <Avatar id={elegidaId} nombre={displayName(elegida, japones)} tamano={22} />
              <span>{displayName(elegida, japones)}</span>
            </span>
          ) : undefined
        }
      />

      <div className="corredora-scroll">
        <div className="buscador">
          <div className="buscador-campo redondeado">
            <IconBuscar className="buscador-icono" />
            <input
              type="search"
              placeholder={t('buscar_corredora')}
              value={filtro}
              onChange={(evento) => setFiltro(evento.target.value)}
            />
            {filtro !== '' ? (
              <button
                type="button"
                className="icon-button"
                onClick={() => {
                  setFiltro('')
                  setElegidaId(-1)
                }}
                aria-label={t('limpiar_todo')}
              >
                <IconCerrar />
              </button>
            ) : null}
          </div>

          {sugerencias.length > 0 ? (
            <ul className="sugerencias">
              {sugerencias.map((c) => {
                const principal = displayName(c, japones)
                const secundario = japones ? c.enName ?? '' : c.jpName ?? ''
                return (
                  <li key={c.charId}>
                    <button
                      type="button"
                      className="sugerencia"
                      onClick={() => {
                        setElegidaId(c.charId)
                        setFiltro('')
                      }}
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

        {elegidaId <= 0 ? (
          <p className="nota">{t('corredora_hint')}</p>
        ) : (
          <DetalleCorredora
            key={`${elegidaId}-${recarga}`}
            modelo={modelo}
            hijoId={elegidaId}
            override={override}
            onRecargarCon={(seleccion) => {
              setOverride(seleccion)
              setRecarga((r) => r + 1)
            }}
            onConteo={setConteoSeleccion}
          />
        )}
      </div>
    </div>
  )
}

function DetalleCorredora({
  modelo,
  hijoId,
  override,
  onRecargarCon,
  onConteo,
}: {
  modelo: AffinityModel
  hijoId: number
  override: Seleccion | null
  onRecargarCon: (seleccion: Seleccion) => void
  onConteo: (conteo: number) => void
}) {
  const { arboles } = useAppStore()
  const { t, japones, codigo } = useI18n()
  const { avisar } = useSnackbar()

  const [seleccionActual, setSeleccionActual] = useState<Seleccion>([])
  const [seleccionOptima, setSeleccionOptima] = useState<Seleccion>([])
  const [sheetSlot, setSheetSlot] = useState<number | null>(null)
  const [mostrarGuardar, setMostrarGuardar] = useState(false)
  const [textoNombre, setTextoNombre] = useState('')

  const { datos: linaje, cargando } = useAsync(() => pedirMejorLinaje(hijoId), [hijoId])

  /* Al llegar el linaje óptimo (o una config externa) se inicializa la
     selección editable en render, sin efecto. */
  const [linajeAplicado, setLinajeAplicado] = useState<Linaje | null>(null)
  if (linaje !== null && linaje !== linajeAplicado) {
    setLinajeAplicado(linaje)
    const optima = listaDeLinaje(linaje)
    setSeleccionOptima(optima)
    setSeleccionActual(override && override.length === 7 ? override : optima)
  }

  useEffect(() => {
    onConteo(seleccionActual.filter((v) => v !== null).length)
  }, [seleccionActual, onConteo])

  const nombreHijo = displayName(modelo.porId(hijoId)!, japones)
  const res = useMemo(
    () => (seleccionActual.length === 7 ? calcularResultado(modelo, seleccionActual) : null),
    [modelo, seleccionActual],
  )
  const totalActual = res?.total ?? 0
  const esOptimo =
    seleccionActual.length === 7 &&
    seleccionOptima.length === 7 &&
    seleccionActual.every((v, i) => v === seleccionOptima[i])

  if (cargando) {
    return (
      <div className="centrado">{t('calculando')}</div>
    )
  }
  if (linaje === null || seleccionActual.length !== 7) {
    return <p className="nota">{t('corredora_invalida')}</p>
  }

  const guardadas = arboles.filter((a) => a.hijoId === hijoId)
  const nombreSugerido = t('nombre_sugerido', nombreHijo, totalActual)

  return (
    <div className="detalle-corredora">
      <section className="card panel-mejor">
        <div className="panel-mejor-cabecera">
          <Avatar id={hijoId} nombre={nombreHijo} tamano={48} />
          <h2>{t('mejor_de', nombreHijo)}</h2>
          <RankPill rango={modelo.rangoTotal(totalActual)} puntos={totalActual} grande />
        </div>

        <hr className="separador" />

        <ChipRol slot={0} seleccion={seleccionActual} />
        {[1, 2, 3, 4, 5, 6].map((slot) => (
          <ChipRol key={slot} slot={slot} seleccion={seleccionActual} onAbrir={() => setSheetSlot(slot)} />
        ))}

        <button
          type="button"
          className="boton-primario"
          onClick={() => {
            store.cargarSeleccion(seleccionActual)
            irA(0)
          }}
        >
          {t('ver_herencia')}
        </button>
      </section>

      {!esOptimo ? (
        <button type="button" className="boton-texto ancho" onClick={() => setSeleccionActual(seleccionOptima)}>
          {t('restablecer_optimo')}
        </button>
      ) : null}

      <ResultadoPanel modelo={modelo} res={res!} japones={japones} />

      <button type="button" className="boton-primario ancho" onClick={() => setMostrarGuardar(true)}>
        {t('guardar_config')}
      </button>

      {guardadas.length > 0 ? (
        <>
          <h3 className="titulo-seccion">{t('guardadas_de', nombreHijo)}</h3>
          <ul className="lista-guardadas">
            {guardadas.map((a) => (
              <li key={a.id}>
                <TarjetaGuardada
                  guardada={a}
                  codigo={codigo}
                  onAbrir={() => onRecargarCon(a.seleccion)}
                  onBorrar={() => {
                    store.eliminarArbol(a.id)
                    avisar(t('eliminado_snack'))
                  }}
                />
              </li>
            ))}
          </ul>
        </>
      ) : null}

      <Dialogo
        open={mostrarGuardar}
        titulo={t('guardar_config')}
        onClose={() => setMostrarGuardar(false)}
      >
        <input
          type="text"
          className="campo-texto"
          placeholder={nombreSugerido}
          value={textoNombre}
          onChange={(evento) => setTextoNombre(evento.target.value)}
          aria-label={t('guardar_config')}
        />
        <button
          type="button"
          className="boton-texto"
          onClick={() => {
            store.guardarArbol(hijoId, textoNombre.trim() === '' ? nombreSugerido : textoNombre, seleccionActual, totalActual)
            setMostrarGuardar(false)
            setTextoNombre('')
            avisar(t('guardado_snack'))
          }}
        >
          {t('guardar')}
        </button>
        <button type="button" className="boton-texto" onClick={() => setMostrarGuardar(false)}>
          {t('cancelar')}
        </button>
      </Dialogo>

      {sheetSlot !== null ? (
        <BottomSheet open onClose={() => setSheetSlot(null)} labelledBy="alternativas-titulo">
          <HojaAlternativas
            key={sheetSlot}
            seleccion={seleccionActual}
            slot={sheetSlot}
            onElegir={(nuevoId) => {
              setSeleccionActual((actual) => {
                const copia = [...actual]
                copia[sheetSlot] = nuevoId
                return copia
              })
              setSheetSlot(null)
            }}
          />
        </BottomSheet>
      ) : null}
    </div>
  )
}

function TarjetaGuardada({
  guardada,
  codigo,
  onAbrir,
  onBorrar,
}: {
  guardada: ArbolGuardado
  codigo: CodigoIdioma
  onAbrir: () => void
  onBorrar: () => void
}) {
  const { t } = useI18n()
  const fecha = new Intl.DateTimeFormat(codigo, { day: '2-digit', month: '2-digit', year: '2-digit' }).format(
    new Date(guardada.creadoEn),
  )
  return (
    <article className="card tarjeta-guardada">
      <button type="button" className="tarjeta-guardada-abrir" onClick={onAbrir}>
        <span className="tarjeta-guardada-nombre">{guardada.nombre}</span>
        <span className="tarjeta-guardada-meta">
          ◎ {guardada.total} · {fecha}
        </span>
      </button>
      <button type="button" className="icon-button" onClick={onBorrar} aria-label={t('cancelar')}>
        <IconCerrar />
      </button>
    </article>
  )
}

function ChipRol({
  slot,
  seleccion,
  onAbrir,
}: {
  slot: number
  seleccion: Seleccion
  onAbrir?: () => void
}) {
  const { modelo } = useAppStore()
  const { t, japones } = useI18n()
  const id = seleccion[slot]
  const personaje = id !== null && modelo ? modelo.porId(id) : null
  const colorRol = colorDeGenealogia(slot)
  const contenido = (
    <>
      {personaje ? <Avatar id={personaje.charId} nombre={displayName(personaje, japones)} tamano={28} /> : null}
      <span className="chip-rol-texto">
        <span className="chip-rol-etiqueta" style={{ color: colorRol }}>
          {t(ETIQUETAS_ROL[slot])}
        </span>
        <span className="chip-rol-nombre">{personaje ? displayName(personaje, japones) : '—'}</span>
      </span>
      {onAbrir ? (
        <span className="chip-rol-flecha" aria-hidden="true">
          ⌄
        </span>
      ) : null}
    </>
  )
  if (!onAbrir) {
    return (
      <div className="chip-rol fijo" style={{ borderColor: 'transparent' }}>
        {contenido}
      </div>
    )
  }
  return (
    <button
      type="button"
      className="chip-rol"
      style={personaje ? { borderColor: `color-mix(in srgb, ${colorRol} 45%, transparent)` } : undefined}
      onClick={onAbrir}
      aria-label={`${t(ETIQUETAS_ROL[slot])}: ${personaje ? displayName(personaje, japones) : '—'}`}
    >
      {contenido}
    </button>
  )
}

/* Mismos umbrales que GruposScreen (par): ◎ ≥20, ○ ≥10, △ ≥4. */
function claseDePuntos(puntos: number): string | null {
  if (puntos >= 20) return 'rank-great'
  if (puntos >= 10) return 'rank-good'
  if (puntos >= 4) return 'rank-fair'
  return null
}

function HojaAlternativas({
  seleccion,
  slot,
  onElegir,
}: {
  seleccion: Seleccion
  slot: number
  onElegir: (id: number) => void
}) {
  const { t, japones } = useI18n()
  const { datos: alternativas } = useAsync(() => pedirAlternativas(seleccion, slot, 20), [slot])
  const ocupanteEtiqueta = t(ETIQUETAS_ROL[slot])

  return (
    <div className="hoja-alternativas">
      <h2 id="alternativas-titulo">{t('alternativas_titulo', ocupanteEtiqueta)}</h2>
      {alternativas === null ? (
        <p className="nota">{t('calculando')}</p>
      ) : alternativas.length === 0 ? (
        <p className="nota">{t('sin_alternativas')}</p>
      ) : (
        <ul>
          {alternativas.map((alt) => (
            <li key={alt.personaje.charId}>
              <AlternativaFila alt={alt} japones={japones} onElegir={onElegir} />
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

function AlternativaFila({
  alt,
  japones,
  onElegir,
}: {
  alt: AlternativaSlot
  japones: boolean
  onElegir: (id: number) => void
}) {
  const nombre = displayName(alt.personaje, japones)
  return (
    <button type="button" className="alternativa" onClick={() => onElegir(alt.personaje.charId)}>
      <Avatar id={alt.personaje.charId} nombre={nombre} tamano={36} />
      <span className="alternativa-texto">
        <span className="alternativa-nombre">{nombre}</span>
        <span className="secundario alternativa-total">Total {alt.total}</span>
      </span>
      <span className="alternativa-puntos" style={{ color: colorDeRango(claseDePuntos(alt.puntosDirectos)) ?? 'var(--texto)' }}>
        {alt.puntosDirectos}pt
      </span>
    </button>
  )
}
