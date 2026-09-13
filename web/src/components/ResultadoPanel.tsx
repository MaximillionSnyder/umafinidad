/* Panel de resultado de compatibilidad (total + vínculos por sección).
   Compartido por Herencia y Mi corredora. */

import type { AffinityModel } from '../domain/affinity'
import { displayName } from '../domain/models'
import { useI18n } from '../i18n'
import { colorDeRango, fondoDeRango } from '../theme/theme'
import { RankPill } from './RankPill'
import { EstadoSeccion, type FilaVinculoUi, type ResultadoCompat } from '../state/resultado'

export function ResultadoPanel({
  modelo,
  res,
  japones,
  idTitulo = 'resultado-titulo',
}: {
  modelo: AffinityModel
  res: ResultadoCompat
  japones: boolean
  idTitulo?: string
}) {
  const { t } = useI18n()
  if (res.vacio) {
    return <p className="nota">{t('elegi_hijo_empezar')}</p>
  }
  const fondoTotal = fondoDeRango(res.rangoTotal?.clase) ?? 'var(--superficie-alta)'
  return (
    <div className="resultado-panel">
      <div className="total-caja" style={{ background: fondoTotal }}>
        <h2 id={idTitulo} className="total-titulo">
          {t('total_herencia')}
        </h2>
        <span
          className="total-numero"
          style={{ color: colorDeRango(res.rangoTotal?.clase) ?? 'var(--texto)' }}
        >
          {res.rangoTotal ? `${res.rangoTotal.simbolo} ` : ''}
          {res.total ?? 0}
        </span>
      </div>

      <SeccionVinculos
        titulo={t('sec_hijo_padres')}
        filas={res.hijoPadres}
        estado={res.estadoHijoPadres}
        modelo={modelo}
        japones={japones}
      />
      <SeccionEntrePadres res={res} modelo={modelo} japones={japones} />
      <SeccionVinculos
        titulo={t('sec_hijo_padres_abuelos')}
        filas={res.hijoPadreAbuelos}
        estado={res.estadoHijoPadreAbuelos}
        modelo={modelo}
        japones={japones}
      />

      {res.notaSinHijo ? <p className="nota">{t('sin_hijo_completa')}</p> : null}
    </div>
  )
}

function NotaEstado({ estado }: { estado: EstadoSeccion }) {
  const { t } = useI18n()
  switch (estado) {
    case EstadoSeccion.FALTA_HIJO:
      return <p className="nota">{t('falta_hijo')}</p>
    case EstadoSeccion.ELIGE_PADRE:
      return <p className="nota">{t('elige_un_padre')}</p>
    case EstadoSeccion.OTRO_PADRE:
      return <p className="nota">{t('elegi_otro_padre')}</p>
    case EstadoSeccion.FALTAN_PADRES:
      return <p className="nota">{t('faltan_padres')}</p>
    case EstadoSeccion.SIN_ABUELOS:
      return <p className="nota">{t('no_hay_abuelos')}</p>
    default:
      return null
  }
}

function SeccionVinculos({
  titulo,
  filas,
  estado,
  modelo,
  japones,
}: {
  titulo: string
  filas: FilaVinculoUi[]
  estado: EstadoSeccion
  modelo: AffinityModel
  japones: boolean
}) {
  return (
    <section className="seccion-vinculos">
      <h3>{titulo}</h3>
      {estado === EstadoSeccion.CON_FILAS ? (
        filas.map((fila, i) => <FilaVinculo key={i} v={fila} modelo={modelo} japones={japones} />)
      ) : (
        <NotaEstado estado={estado} />
      )}
    </section>
  )
}

function SeccionEntrePadres({ res, modelo, japones }: { res: ResultadoCompat; modelo: AffinityModel; japones: boolean }) {
  const { t } = useI18n()
  return (
    <section className="seccion-vinculos">
      <h3>{t('sec_entre_padres')}</h3>
      {res.estadoEntrePadres === EstadoSeccion.CON_FILAS && res.entrePadres ? (
        <FilaVinculo v={res.entrePadres} modelo={modelo} japones={japones} />
      ) : (
        <NotaEstado estado={res.estadoEntrePadres} />
      )}
    </section>
  )
}

function FilaVinculo({ v, modelo, japones }: { v: FilaVinculoUi; modelo: AffinityModel; japones: boolean }) {
  const { t } = useI18n()
  const nombres = v.ids
    .map((id) => {
      const c = modelo.porId(id)
      return c ? displayName(c, japones) : String(id)
    })
    .join(' × ')
  return (
    <article className="fila-vinculo">
      <div className="fila-vinculo-cabecera">
        <span className="fila-vinculo-nombres">{nombres}</span>
        <RankPill rango={v.rango} puntos={v.puntos} />
      </div>
      {v.esCorredora ? <p className="nota compacta">{t('corredora_nota')}</p> : null}
    </article>
  )
}
