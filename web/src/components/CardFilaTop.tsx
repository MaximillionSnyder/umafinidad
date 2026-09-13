/* Fila del top de linajes, compartida por el Top global ("Mejores
   linajes") y "Mis Umas". Espejo de ui/componentes/FilaTop.kt. */

import type { AffinityModel, Linaje } from '../domain/affinity'
import { displayName } from '../domain/models'
import { useI18n } from '../i18n'
import { colorDeMedalla } from '../theme/theme'
import { RankPill } from './RankPill'
import { Medalla } from './Medalla'

interface CardFilaTopProps {
  i: number
  combo: Linaje
  modelo: AffinityModel
  onVerHerencia: () => void
}

export function CardFilaTop({ i, combo, modelo, onVerHerencia }: CardFilaTopProps) {
  const { t, japones } = useI18n()
  const nombres = [combo.hijo, combo.padre, combo.madre].map((c) => displayName(c, japones)).join(' × ')
  const colorMedalla = colorDeMedalla(i)

  return (
    <button
      type="button"
      className="card clickable fila-top"
      onClick={onVerHerencia}
      aria-label={`${nombres}. ${t('ver_herencia')}`}
      style={
        i < 3
          ? {
              background: 'color-mix(in srgb, var(--contenedor-primario) 25%, transparent)',
              borderColor: colorMedalla ? `color-mix(in srgb, ${colorMedalla} 60%, transparent)` : undefined,
            }
          : undefined
      }
    >
      <Medalla pos={i} />
      <span className="fila-top-texto">
        <span className="fila-top-nombres">{nombres}</span>
        <span className="fila-top-link">{t('ver_herencia')}</span>
      </span>
      <RankPill rango={modelo.rangoTotal(combo.puntos)} puntos={combo.puntos} />
    </button>
  )
}
