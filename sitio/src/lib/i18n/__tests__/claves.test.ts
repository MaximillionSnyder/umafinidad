/* Guarda de i18n: las claves del panel "Más info" de Mi corredora tienen que
   existir en los 9 idiomas. Los locales de la web se generan desde los
   strings.xml de la app Android (npm run strings). */

import { describe, expect, it } from 'vitest'
import en from '../locales/en.json'
import es from '../locales/es.json'
import ja from '../locales/ja.json'
import zhCN from '../locales/zh-CN.json'
import zhTW from '../locales/zh-TW.json'
import ko from '../locales/ko.json'
import id from '../locales/id.json'
import th from '../locales/th.json'
import vi from '../locales/vi.json'

const CLAVES = [
  'afinidad_mas_info',
  'afinidad_info_titulo',
  'afinidad_info_intro',
  'afinidad_info_filas',
  'afinidad_info_medio_punto',
  'afinidad_info_cambios',
  'afinidad_info_cambios_desc',
]

const LOCALES: Record<string, Record<string, string>> = {
  en,
  es,
  ja,
  'zh-CN': zhCN,
  'zh-TW': zhTW,
  ko,
  id,
  th,
  vi,
}

describe('i18n · Más info de afinidad', () => {
  it('las claves nuevas existen en los 9 idiomas', () => {
    for (const [codigo, tabla] of Object.entries(LOCALES)) {
      for (const clave of CLAVES) {
        expect(tabla[clave], `${codigo} · ${clave}`).toBeTruthy()
      }
    }
  })
})
