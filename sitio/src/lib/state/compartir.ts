/* Compartir la selección actual por URL. Formato compacto y legible:
   cada slot es un id o vacío, separados por "-" (los vacíos finales se
   recortan). Ej.: 1001-1002-1003----  →  "1001-1002-1003". */

import { SLOTS, type Seleccion } from '../domain/herencia'

export const PARAM_SELECCION = 's'

export function codificarSeleccion(seleccion: Seleccion): string {
  const partes = seleccion.map((id) => (id === null ? '' : String(id)))
  while (partes.length > 0 && partes[partes.length - 1] === '') partes.pop()
  return partes.join('-')
}

/* Devuelve null si el parámetro no es válido (formato o ids inexistentes). */
export function decodificarSeleccion(
  raw: string | null,
  existe: (id: number) => boolean,
): Seleccion | null {
  if (raw === null || raw === '') return null
  const partes = raw.split('-')
  if (partes.length > SLOTS) return null
  const seleccion: Seleccion = Array(SLOTS).fill(null)
  for (let i = 0; i < partes.length; i++) {
    const parte = partes[i]
    if (parte === '') continue
    if (!/^\d+$/.test(parte)) return null
    const id = Number(parte)
    if (!Number.isSafeInteger(id) || id <= 0) return null
    if (!existe(id)) return null
    seleccion[i] = id
  }
  return seleccion
}

/* Enlace absoluto para compartir, conservando ruta y base actuales. */
export function enlaceSeleccion(pathname: string, seleccion: Seleccion): string {
  const codificada = codificarSeleccion(seleccion)
  const url = new URL(pathname, window.location.origin)
  if (codificada !== '') url.searchParams.set(PARAM_SELECCION, codificada)
  return url.toString()
}
