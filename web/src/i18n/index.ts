/* i18n: locales generados desde los strings.xml de la app Android
   (scripts/extract-strings.mjs). Resolución: preferencia del usuario →
   idioma del navegador → inglés (default de la app). */

import { useMemo } from 'react'
import { useAppStore } from '../state/store'
import { codigoIdioma, Idioma } from '../data/prefs'
import en from './locales/en.json'
import es from './locales/es.json'
import ja from './locales/ja.json'

export type CodigoIdioma = 'en' | 'es' | 'ja'

const LOCALES: Record<CodigoIdioma, Record<string, string>> = { en, es, ja }

export function resolverIdioma(idioma: Idioma): CodigoIdioma {
  const codigo = codigoIdioma(idioma)
  if (codigo !== null) return codigo
  const navegador = typeof navigator !== 'undefined' ? navigator.language.slice(0, 2).toLowerCase() : 'en'
  return navegador === 'es' || navegador === 'ja' ? navegador : 'en'
}

export function formatear(plantilla: string, args: (string | number)[]): string {
  return plantilla.replace(/\{(\d+)\}/g, (_, n) => {
    const valor = args[Number(n)]
    return valor === undefined ? '' : String(valor)
  })
}

export interface I18n {
  codigo: CodigoIdioma
  japones: boolean
  t: (clave: string, ...args: (string | number)[]) => string
}

export function useI18n(): I18n {
  const { idioma } = useAppStore()
  return useMemo(() => {
    const codigo = resolverIdioma(idioma)
    const tabla = LOCALES[codigo]
    const t = (clave: string, ...args: (string | number)[]) =>
      formatear(tabla[clave] ?? LOCALES.en[clave] ?? clave, args)
    return { codigo, japones: codigo === 'ja', t }
  }, [idioma])
}
