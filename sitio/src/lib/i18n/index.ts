/* i18n: locales generados desde los strings.xml de la app Android
   (scripts/extract-strings.mjs). Resolución: preferencia del usuario →
   idioma del navegador → inglés (default de la app). */

import { codigoIdioma, Idioma } from '../data/prefs'
import en from './locales/en.json'
import es from './locales/es.json'
import ja from './locales/ja.json'
import zhCN from './locales/zh-CN.json'
import zhTW from './locales/zh-TW.json'
import ko from './locales/ko.json'
import id from './locales/id.json'
import th from './locales/th.json'
import vi from './locales/vi.json'
import { EXTRA } from './extra'

export type CodigoIdioma =
  | 'en'
  | 'es'
  | 'ja'
  | 'zh-CN'
  | 'zh-TW'
  | 'ko'
  | 'id'
  | 'th'
  | 'vi'

const LOCALES: Record<CodigoIdioma, Record<string, string>> = {
  en: { ...en, ...EXTRA.en },
  es: { ...es, ...EXTRA.es },
  ja: { ...ja, ...EXTRA.ja },
  'zh-CN': { ...zhCN, ...EXTRA['zh-CN'] },
  'zh-TW': { ...zhTW, ...EXTRA['zh-TW'] },
  ko: { ...ko, ...EXTRA.ko },
  id: { ...id, ...EXTRA.id },
  th: { ...th, ...EXTRA.th },
  vi: { ...vi, ...EXTRA.vi },
}

/* Códigos de navegador a locales soportados (chino por escritura). */
function idiomaDelNavegador(tag: string): CodigoIdioma {
  const t = tag.toLowerCase()
  if (t.startsWith('es')) return 'es'
  if (t.startsWith('ja')) return 'ja'
  if (t.startsWith('ko')) return 'ko'
  if (t.startsWith('id') || t.startsWith('in')) return 'id'
  if (t.startsWith('th')) return 'th'
  if (t.startsWith('vi')) return 'vi'
  if (t.startsWith('zh')) {
    if (t.includes('tw') || t.includes('hk') || t.includes('mo') || t.includes('hant')) {
      return 'zh-TW'
    }
    return 'zh-CN'
  }
  return 'en'
}

export function resolverIdioma(idioma: Idioma): CodigoIdioma {
  const codigo = codigoIdioma(idioma)
  if (codigo !== null) return codigo
  const navegador =
    typeof navigator !== 'undefined' ? navigator.language : 'en'
  return idiomaDelNavegador(navegador)
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

export function crearI18n(idioma: Idioma): I18n {
  const codigo = resolverIdioma(idioma)
  const tabla = LOCALES[codigo]
  const t = (clave: string, ...args: (string | number)[]) =>
    formatear(tabla[clave] ?? LOCALES.en[clave] ?? clave, args)
  return { codigo, japones: codigo === 'ja', t }
}
