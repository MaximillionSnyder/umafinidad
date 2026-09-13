/* Medalla del top 3 (oro/plata/bronce) o número para el resto. */

import { colorDeMedalla } from '../theme/theme'

export function Medalla({ pos }: { pos: number }) {
  const color = colorDeMedalla(pos)
  if (color === null) {
    return <span className="medalla numero">{pos + 1}</span>
  }
  return (
    <span className="medalla" style={{ background: color, color: '#14161A' }}>
      {pos + 1}
    </span>
  )
}
