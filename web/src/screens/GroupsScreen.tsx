/* Grupos de afinidad datamined con filtro por puntos y lista expandible.
   Porte de ui/groups/GroupsScreen.kt. */

import { useState } from 'react'
import { useAppStore } from '../state/store'
import { useI18n } from '../i18n'
import { displayName } from '../domain/models'
import { colorDeRango } from '../theme/theme'
import { HeaderBarConVolver } from '../components/HeaderBar'

const OPCIONES = [0, 2, 5, 7, 8]

/* Mismos umbrales de rango que el modelo (par): ◎ ≥20, ○ ≥10, △ ≥4. */
function claseDePuntos(puntos: number): string | null {
  if (puntos >= 20) return 'rank-great'
  if (puntos >= 10) return 'rank-good'
  if (puntos >= 4) return 'rank-fair'
  return null
}

export function GroupsScreen({ onVolver }: { onVolver: () => void }) {
  const { modelo } = useAppStore()
  const { t, japones } = useI18n()
  const [min, setMin] = useState(0)
  const [grupoAbierto, setGrupoAbierto] = useState<number | null>(null)

  if (!modelo) return <div className="centrado">{t('calculando')}</div>

  const grupos = modelo.todosLosGrupos().filter((g) => g.puntos >= min)

  return (
    <div className="pantalla">
      <HeaderBarConVolver titulo={t('tab_groups')} onVolver={onVolver} />

      <div className="chips-filtro" role="group" aria-label={t('filtro_todos')}>
        {OPCIONES.map((valor) => (
          <button
            key={valor}
            type="button"
            className={valor === min ? 'filtro-chip activo' : 'filtro-chip'}
            aria-pressed={valor === min}
            onClick={() => setMin(valor)}
          >
            {valor === 0 ? t('filtro_todos') : t('filtro_pt', valor)}
          </button>
        ))}
      </div>

      {grupos.length === 0 ? (
        <div className="centrado">{t('sin_grupos_filtro')}</div>
      ) : (
        <ul className="lista-grupos">
          {grupos.map((grupo) => {
            const miembros = modelo.miembrosDeGrupo(grupo.tipo)
            const abierto = grupoAbierto === grupo.tipo
            const estadoTxt = abierto ? t('expandido') : t('contraido')
            return (
              <li key={grupo.tipo}>
                <article className={abierto ? 'card grupo abierto' : 'card grupo'}>
                  <button
                    type="button"
                    className="grupo-cabecera"
                    aria-expanded={abierto}
                    aria-describedby={`estado-grupo-${grupo.tipo}`}
                    onClick={() => setGrupoAbierto(abierto ? null : grupo.tipo)}
                  >
                    <span id={`estado-grupo-${grupo.tipo}`} className="oculto-visualmente">
                      {estadoTxt}
                    </span>
                    <span className="grupo-id">#{grupo.tipo}</span>
                    <span className="secundario grupo-cantidad">{t('miembros_cantidad', miembros.length)}</span>
                    <span
                      className="grupo-puntos"
                      style={{ color: colorDeRango(claseDePuntos(grupo.puntos)) ?? 'var(--texto)' }}
                    >
                      {grupo.puntos}pt
                    </span>
                  </button>
                  {abierto ? (
                    <div className="grupo-miembros">
                      {miembros.map((m) => (
                        <span key={m.charId} className="miembro">
                          {displayName(m, japones)}
                        </span>
                      ))}
                    </div>
                  ) : null}
                </article>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}
