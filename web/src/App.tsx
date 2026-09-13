/* Shell de la app: tema, carga de datos, navegación (5 tabs + overlays
   Grupos/Ranking) con hash routing para que el botón atrás del navegador
   funcione como en Android. */

import { useEffect, useState } from 'react'
import { store, useAppStore } from './state/store'
import { useTema } from './theme/theme'
import { useI18n } from './i18n'
import { SnackbarProvider } from './components/Snackbar'
import { CompatScreen } from './screens/CompatScreen'
import { TopLinajesScreen } from './screens/TopLinajesScreen'
import { CorredoraScreen } from './screens/CorredoraScreen'
import { ElencoScreen } from './screens/ElencoScreen'
import { SettingsScreen } from './screens/SettingsScreen'
import { GroupsScreen } from './screens/GroupsScreen'
import { RankingScreen } from './screens/RankingScreen'
import { BienvenidaAccesibilidad } from './components/BienvenidaAccesibilidad'
import { IconAjustes, IconCorredora, IconCorazon, IconElenco, IconTop } from './components/Icons'
import { ESCALA_TAMANO } from './data/prefs'
import { irA, volver } from './state/navegacion'

type Ruta =
  | { tipo: 'tab'; pagina: number }
  | { tipo: 'overlay'; nombre: 'grupos' | 'ranking' | 'ranking-padres' }

function parsearHash(hash: string): Ruta {
  const limpio = hash.replace(/^#\/?/, '')
  if (limpio === 'grupos') return { tipo: 'overlay', nombre: 'grupos' }
  if (limpio === 'ranking') return { tipo: 'overlay', nombre: 'ranking' }
  if (limpio === 'ranking-padres') return { tipo: 'overlay', nombre: 'ranking-padres' }
  const pagina = Number(limpio.replace('tab/', ''))
  return { tipo: 'tab', pagina: Number.isInteger(pagina) && pagina >= 0 && pagina <= 4 ? pagina : 0 }
}

function useRuta(): Ruta {
  const [ruta, setRuta] = useState<Ruta>(() => parsearHash(window.location.hash))
  useEffect(() => {
    const onHash = () => setRuta(parsearHash(window.location.hash))
    window.addEventListener('hashchange', onHash)
    return () => window.removeEventListener('hashchange', onHash)
  }, [])
  return ruta
}

function TabBoton({
  pagina,
  activo,
  etiqueta,
  icono,
}: {
  pagina: number
  activo: boolean
  etiqueta: string
  icono: React.ReactNode
}) {
  return (
    <button
      type="button"
      className={activo ? 'tab activo' : 'tab'}
      aria-current={activo ? 'page' : undefined}
      onClick={() => irA(pagina)}
    >
      {icono}
      <span>{etiqueta}</span>
    </button>
  )
}

function Contenido() {
  const { modelo, mostrarBienvenida } = useAppStore()
  const ruta = useRuta()
  const { t, codigo } = useI18n()

  useEffect(() => {
    document.documentElement.lang = codigo
  }, [codigo])

  if (modelo === null) {
    return <div className="centrado">{t('calculando')}</div>
  }

  if (ruta.tipo === 'overlay') {
    if (ruta.nombre === 'grupos') return <GroupsScreen onVolver={volver} />
    return (
      <RankingScreen
        key={ruta.nombre}
        onVolver={volver}
        modoInicial={ruta.nombre === 'ranking-padres' ? 'PADRES' : 'VERSATIL'}
      />
    )
  }

  return (
    <>
      <div className="contenido">
        {ruta.pagina === 0 ? <CompatScreen /> : null}
        {ruta.pagina === 1 ? <TopLinajesScreen /> : null}
        {ruta.pagina === 2 ? <CorredoraScreen /> : null}
        {ruta.pagina === 3 ? <ElencoScreen /> : null}
        {ruta.pagina === 4 ? <SettingsScreen /> : null}
      </div>
      <nav className="bottom-nav" aria-label={t('app_name')}>
        <TabBoton pagina={0} activo={ruta.pagina === 0} etiqueta={t('tab_compat')} icono={<IconCorazon />} />
        <TabBoton pagina={1} activo={ruta.pagina === 1} etiqueta={t('tab_top')} icono={<IconTop />} />
        <TabBoton pagina={2} activo={ruta.pagina === 2} etiqueta={t('tab_corredora')} icono={<IconCorredora />} />
        <TabBoton pagina={3} activo={ruta.pagina === 3} etiqueta={t('tab_elenco')} icono={<IconElenco />} />
        <TabBoton pagina={4} activo={ruta.pagina === 4} etiqueta={t('tab_mas')} icono={<IconAjustes />} />
      </nav>
      {mostrarBienvenida ? <BienvenidaAccesibilidad /> : null}
    </>
  )
}

export default function App() {
  const tema = useTema()
  const { tamanoTexto, textoNegrita } = useAppStore()

  useEffect(() => {
    void store.init()
  }, [])

  useEffect(() => {
    document.documentElement.dataset.theme = tema
  }, [tema])

  useEffect(() => {
    const escala = ESCALA_TAMANO[tamanoTexto] ?? 1
    document.documentElement.style.setProperty('--escala-texto', String(escala))
  }, [tamanoTexto])

  useEffect(() => {
    document.body.classList.toggle('negrita', textoNegrita)
  }, [textoNegrita])

  return (
    <SnackbarProvider>
      <div className="app-shell">
        <Contenido />
      </div>
    </SnackbarProvider>
  )
}
