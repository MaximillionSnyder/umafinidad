/* "Mis Umas": elenco propio. Solapa 1: editar roster. Solapa 2: mejores
   linajes dentro del elenco. Porte de ui/elenco/ElencoScreen.kt. */

import { useState } from 'react'
import { displayName } from '../domain/models'
import { coincideDifuso } from '../domain/busqueda'
import { store, useAppStore } from '../state/store'
import { useI18n } from '../i18n'
import { irA } from '../state/navegacion'
import { pedirTopLinajesDeElenco } from '../worker/cliente'
import { useAsync } from '../state/useAsync'
import { Avatar } from '../components/Avatar'
import { HeaderBar } from '../components/HeaderBar'
import { CardFilaTop } from '../components/CardFilaTop'
import { Dialogo } from '../components/Modal'
import { IconBuscar, IconCerrar } from '../components/Icons'

export function ElencoScreen() {
  const { modelo, elenco } = useAppStore()
  const { t } = useI18n()
  const [tab, setTab] = useState(0)

  if (!modelo) return <div className="centrado">{t('calculando')}</div>

  const totalJugables = modelo.personajes.filter((c) => c.playable === true && c.active === true).length

  return (
    <div className="pantalla">
      <HeaderBar titulo={t('tab_elenco')} pillTexto={t('elenco_contador', elenco.size, totalJugables)} />

      <div className="tab-row" role="tablist" aria-label={t('tab_elenco')}>
        <button
          type="button"
          role="tab"
          aria-selected={tab === 0}
          className={tab === 0 ? 'tab-item activo' : 'tab-item'}
          onClick={() => setTab(0)}
        >
          {t('elenco_tab_editar')}
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={tab === 1}
          className={tab === 1 ? 'tab-item activo' : 'tab-item'}
          onClick={() => setTab(1)}
        >
          {t('elenco_tab_linajes')}
        </button>
      </div>

      {tab === 0 ? <EditorElenco /> : <LinajesElenco key={[...elenco].sort((a, b) => a - b).join(',')} />}
    </div>
  )
}

function EditorElenco() {
  const { modelo, elenco } = useAppStore()
  const { t, japones } = useI18n()
  const [filtro, setFiltro] = useState('')
  const [confirmarLimpiar, setConfirmarLimpiar] = useState(false)

  if (!modelo) return null

  const jugables = modelo.personajes.filter((c) => c.playable === true && c.active === true)
  const filtrados = jugables.filter((c) => coincideDifuso(c, filtro))

  return (
    <div className="elenco-editor">
      <div className="buscador">
        <div className="buscador-campo">
          <IconBuscar className="buscador-icono" />
          <input
            type="search"
            placeholder={t('buscar')}
            value={filtro}
            onChange={(evento) => setFiltro(evento.target.value)}
          />
          {filtro !== '' ? (
            <button type="button" className="icon-button" onClick={() => setFiltro('')} aria-label={t('limpiar_todo')}>
              <IconCerrar />
            </button>
          ) : null}
        </div>
      </div>

      <div className="elenco-acciones">
        <button type="button" className="boton-texto" onClick={() => store.marcarElenco(filtrados.map((c) => c.charId))}>
          {t('elenco_marcar_visibles')}
        </button>
        <button
          type="button"
          className="boton-texto peligro"
          onClick={() => {
            if (elenco.size > 0) setConfirmarLimpiar(true)
          }}
        >
          {t('limpiar_todo')}
        </button>
      </div>

      {filtrados.length === 0 ? (
        <div className="centrado">{t('sin_resultados')}</div>
      ) : (
        <ul className="grilla-tarjetas grilla-elenco">
          {filtrados.map((c) => {
            const nombre = displayName(c, japones)
            const marcado = elenco.has(c.charId)
            return (
              <li key={c.charId}>
                <button
                  type="button"
                  role="checkbox"
                  aria-checked={marcado}
                  className={marcado ? 'card-tarjeta seleccionada' : 'card-tarjeta'}
                  onClick={() => store.toggleElenco(c.charId)}
                >
                  <span className="avatar-envoltura">
                    <Avatar id={c.charId} nombre={nombre} tamano={56} />
                    {marcado ? (
                      <span className="check" aria-hidden="true">
                        ✓
                      </span>
                    ) : null}
                  </span>
                  <span className="nombre-principal">{nombre}</span>
                </button>
              </li>
            )
          })}
        </ul>
      )}

      <Dialogo
        open={confirmarLimpiar}
        titulo={t('elenco_limpiar_titulo')}
        descripcion={t('elenco_limpiar_mensaje')}
        onClose={() => setConfirmarLimpiar(false)}
      >
        <button
          type="button"
          className="boton-texto"
          onClick={() => {
            store.limpiarElenco()
            setConfirmarLimpiar(false)
          }}
        >
          {t('limpiar_todo')}
        </button>
        <button type="button" className="boton-texto" onClick={() => setConfirmarLimpiar(false)}>
          {t('cancelar')}
        </button>
      </Dialogo>
    </div>
  )
}

function LinajesElenco() {
  const { modelo, elenco } = useAppStore()
  const { t } = useI18n()
  const { datos: linajes } = useAsync(() => pedirTopLinajesDeElenco([...elenco], 40), [])

  if (!modelo) return null

  if (linajes === null) {
    return <div className="centrado">{t('calculando')}</div>
  }

  if (linajes.length === 0) {
    return (
      <div className="centrado">
        <p className="nota centrada">{t('elenco_minimo')}</p>
      </div>
    )
  }

  return (
    <ul className="lista-top">
      {linajes.map((combo, i) => (
        <li key={`${combo.hijo.charId}-${i}`}>
          <CardFilaTop
            i={i}
            combo={combo}
            modelo={modelo}
            onVerHerencia={() => {
              store.cargarLinaje(combo)
              irA(0)
            }}
          />
        </li>
      ))}
    </ul>
  )
}

