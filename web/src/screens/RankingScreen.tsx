/* Ranking con dos modos: "Umas más versátiles" (total afinidad) y
   "Mejores padres" (veces como padre óptimo + % + media). Porte de
   ui/ranking/RankingScreen.kt. */

import { useEffect, useState } from 'react'
import type { RankingAfinidad, RankingPadre } from '../domain/affinity'
import { useAppStore } from '../state/store'
import { useI18n } from '../i18n'
import { displayName } from '../domain/models'
import { pedirRankingAfinidad, pedirRankingPadres } from '../worker/cliente'
import { HeaderBarConVolver } from '../components/HeaderBar'
import { RankPill } from '../components/RankPill'
import { Medalla } from '../components/Medalla'
import { Avatar } from '../components/Avatar'
import { Dialogo } from '../components/Modal'
import { IconInfo } from '../components/Icons'
import { colorDeMedalla } from '../theme/theme'

export type ModoRanking = 'VERSATIL' | 'PADRES'

export function RankingScreen({ onVolver, modoInicial = 'VERSATIL' }: { onVolver: () => void; modoInicial?: ModoRanking }) {
  const { modelo } = useAppStore()
  const { t } = useI18n()
  const [modo, setModo] = useState<ModoRanking>(modoInicial)
  const [mostrarAyuda, setMostrarAyuda] = useState(false)
  const [ranking, setRanking] = useState<RankingAfinidad[] | null>(null)
  const [rankingPadres, setRankingPadres] = useState<RankingPadre[] | null>(null)

  useEffect(() => {
    if (!modelo) return
    let activo = true
    void Promise.all([pedirRankingAfinidad(), pedirRankingPadres()]).then(([a, p]) => {
      if (!activo) return
      setRanking(a)
      setRankingPadres(p)
    })
    return () => {
      activo = false
    }
  }, [modelo])

  if (!modelo) return <div className="centrado">{t('calculando')}</div>

  return (
    <div className="pantalla">
      <HeaderBarConVolver titulo={t('tab_ranking')} onVolver={onVolver} />

      <div className="segmentado" role="group" aria-label={t('tab_ranking')}>
        <button
          type="button"
          className={modo === 'VERSATIL' ? 'segmento activo' : 'segmento'}
          aria-pressed={modo === 'VERSATIL'}
          onClick={() => setModo('VERSATIL')}
        >
          {t('ranking_modo_versatil')}
        </button>
        <button
          type="button"
          className={modo === 'PADRES' ? 'segmento activo' : 'segmento'}
          aria-pressed={modo === 'PADRES'}
          onClick={() => setModo('PADRES')}
        >
          {t('ranking_modo_padres')}
        </button>
      </div>

      {modo === 'VERSATIL' ? (
        ranking === null ? (
          <div className="centrado">{t('calculando')}</div>
        ) : ranking.length === 0 ? (
          <div className="centrado">{t('sin_datos')}</div>
        ) : (
          <ul className="lista-ranking">
            {ranking.map((entry, i) => (
              <li key={entry.personaje.charId}>
                <CardFilaRanking pos={i} entry={entry} modelo={modelo} />
              </li>
            ))}
          </ul>
        )
      ) : (
        <>
          <button
            type="button"
            className="ayuda-ranking"
            onClick={() => setMostrarAyuda(true)}
            aria-label={t('ranking_padres_ayuda_titulo')}
          >
            <span className="secundario">{t('ranking_padres_ayuda_corta')}</span>
            <IconInfo className="icono-info" />
          </button>
          {rankingPadres === null ? (
            <div className="centrado">{t('calculando')}</div>
          ) : rankingPadres.length === 0 ? (
            <div className="centrado">{t('sin_datos')}</div>
          ) : (
            <ul className="lista-ranking">
              {rankingPadres.map((entry, i) => (
                <li key={entry.personaje.charId}>
                  <CardFilaPadre pos={i} entry={entry} />
                </li>
              ))}
            </ul>
          )}
        </>
      )}

      <Dialogo
        open={mostrarAyuda}
        titulo={t('ranking_padres_ayuda_titulo')}
        descripcion={t('ranking_padres_ayuda_larga')}
        onClose={() => setMostrarAyuda(false)}
      >
        <button type="button" className="boton-texto" onClick={() => setMostrarAyuda(false)}>
          {t('entendido')}
        </button>
      </Dialogo>
    </div>
  )
}

function estiloTop(pos: number): React.CSSProperties {
  if (pos >= 3) return {}
  const color = colorDeMedalla(pos)
  return {
    background: 'color-mix(in srgb, var(--contenedor-primario) 25%, transparent)',
    borderColor: color ? `color-mix(in srgb, ${color} 60%, transparent)` : undefined,
  }
}

function CardFilaRanking({ pos, entry, modelo }: { pos: number; entry: RankingAfinidad; modelo: import('../domain/affinity').AffinityModel }) {
  const { japones } = useI18n()
  const nombre = displayName(entry.personaje, japones)
  return (
    <article className="card fila-ranking" style={estiloTop(pos)}>
      <Medalla pos={pos} />
      <Avatar id={entry.personaje.charId} nombre={nombre} tamano={36} />
      <span className="fila-ranking-nombre">{nombre}</span>
      <RankPill rango={modelo.rangoRanking(entry.total)} puntos={entry.total} />
    </article>
  )
}

function CardFilaPadre({ pos, entry }: { pos: number; entry: RankingPadre }) {
  const { t, japones } = useI18n()
  const nombre = displayName(entry.personaje, japones)
  return (
    <article className="card fila-ranking" style={estiloTop(pos)}>
      <Medalla pos={pos} />
      <Avatar id={entry.personaje.charId} nombre={nombre} tamano={36} />
      <span className="fila-ranking-texto">
        <span className="fila-ranking-nombre">{nombre}</span>
        <span className="secundario fila-ranking-media">{t('ranking_padres_media', entry.puntosMedios)}</span>
      </span>
      <span className="fila-ranking-veces">{t('ranking_padres_veces', entry.veces, Math.trunc(entry.porcentaje))}</span>
    </article>
  )
}
