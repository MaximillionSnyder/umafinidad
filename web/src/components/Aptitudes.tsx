/* Aptitudes (pista / distancia / estilo). Espejo de
   ui/componentes/Aptitudes.kt. */

import { useI18n } from '../i18n'
import { colorDeLetra } from '../theme/theme'
import { aptitudesDestacadas } from '../domain/aptitudes'

const ETIQUETAS = [
  'apt_turf',
  'apt_dirt',
  'apt_corta',
  'apt_milla',
  'apt_media',
  'apt_larga',
  'apt_fuga',
  'apt_vanguardia',
  'apt_remate',
  'apt_retraso',
]

/* Chip "Césped A". tintado: fondo con el color de la letra (variante
   compacta de las grillas); si no, fondo neutro (detalle completo). */
function ChipAptitud({ indice, letra, tintado = false }: { indice: number; letra: string; tintado?: boolean }) {
  const { t } = useI18n()
  return (
    <span
      className="chip"
      style={tintado ? { background: `color-mix(in srgb, ${colorDeLetra(letra)} 16%, transparent)` } : undefined}
    >
      <span className="secundario">{t(ETIQUETAS[indice])}</span>
      <span className="letra" style={{ color: colorDeLetra(letra) }}>
        {letra}
      </span>
    </span>
  )
}

const SECCIONES: [string, [number, number]][] = [
  ['apt_pista', [0, 1]],
  ['apt_distancia', [2, 5]],
  ['apt_estilo', [6, 9]],
]

/* Detalle completo: una fila por categoría con las 10 aptitudes. */
export function AptitudesDetalle({ apt }: { apt: string[] }) {
  const { t } = useI18n()
  return (
    <div className="aptitudes-detalle">
      {SECCIONES.map(([titulo, [desde, hasta]]) => (
        <div key={titulo} className="aptitudes-grupo">
          <h4>{t(titulo)}</h4>
          <div className="aptitudes-flow">
            {Array.from({ length: hasta - desde + 1 }, (_, k) => desde + k).map((i) => (
              <ChipAptitud key={i} indice={i} letra={apt[i]} />
            ))}
          </div>
        </div>
      ))}
    </div>
  )
}

/* Variante compacta: solo las destacadas (A o B), chips tintados. */
export function AptitudesChips({ apt }: { apt: string[] }) {
  const destacadas = aptitudesDestacadas(apt)
  if (destacadas.length === 0) return null
  return (
    <div className="aptitudes-flow">
      {destacadas.map((i) => (
        <ChipAptitud key={i} indice={i} letra={apt[i]} tintado />
      ))}
    </div>
  )
}
