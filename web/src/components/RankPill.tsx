/* Pastilla "◎ 257" con fondo tintado al 12% del color del rango
   (idéntico al estilo .puntos de la web original). */

import type { Rango } from '../domain/affinity'
import { colorDeRango, fondoDeRango } from '../theme/theme'

interface RankPillProps {
  rango: Rango | null
  puntos: number
  grande?: boolean
}

export function RankPill({ rango, puntos, grande = false }: RankPillProps) {
  const frente = colorDeRango(rango?.clase) ?? 'var(--texto)'
  const fondo = fondoDeRango(rango?.clase) ?? 'var(--superficie-alta)'
  return (
    <span className={grande ? 'rank-pill grande' : 'rank-pill'} style={{ color: frente, background: fondo }}>
      {rango ? `${rango.simbolo} ` : ''}
      {puntos}
    </span>
  )
}

/* Versión simple sin pastilla (texto coloreado). */
export function PuntosRango({ rango, puntos }: RankPillProps) {
  const color = colorDeRango(rango?.clase) ?? 'var(--texto)'
  return (
    <span style={{ color, fontWeight: 700 }}>
      {rango ? `${rango.simbolo} ` : ''}
      {puntos}
    </span>
  )
}
