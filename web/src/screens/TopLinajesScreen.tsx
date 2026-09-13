/* Tab "Mejores linajes": top 20 de linajes completos con cálculo
   diferido (worker). Porte de ui/top/TopLinajesScreen.kt. */

import { useEffect, useState } from 'react'
import type { Linaje } from '../domain/affinity'
import { store, useAppStore } from '../state/store'
import { useI18n } from '../i18n'
import { irA } from '../state/navegacion'
import { pedirTopLinajes } from '../worker/cliente'
import { HeaderBar } from '../components/HeaderBar'
import { CardFilaTop } from '../components/CardFilaTop'

export function TopLinajesScreen() {
  const { modelo } = useAppStore()
  const { t } = useI18n()
  const [top, setTop] = useState<Linaje[] | null>(null)

  useEffect(() => {
    if (!modelo) return
    let activo = true
    void pedirTopLinajes(20).then((resultado) => {
      if (activo) setTop(resultado)
    })
    return () => {
      activo = false
    }
  }, [modelo])

  if (top === null || modelo === null) {
    return (
      <div className="pantalla">
        <HeaderBar titulo={t('tab_top')} />
        <div className="centrado">{t('calculando')}</div>
      </div>
    )
  }

  if (top.length === 0) {
    return (
      <div className="pantalla">
        <HeaderBar titulo={t('tab_top')} />
        <div className="centrado">{t('sin_datos')}</div>
      </div>
    )
  }

  return (
    <div className="pantalla">
      <HeaderBar titulo={t('tab_top')} pillTexto={`${top.length} ${t('tab_top').toLowerCase()}`} />
      <ul className="lista-top">
        {top.map((combo, i) => (
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
    </div>
  )
}
