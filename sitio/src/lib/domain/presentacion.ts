/* SOLO presentación de la pantalla "Mi corredora": reparte el total de
   afinidad entre las filas del árbol para que la suma de las filas sea
   exactamente el total. NO interviene en ningún cálculo: el total, los
   rankings, las alternativas y la herencia siguen usando los vínculos
   reales (totalDeSeleccion, puntajePar, puntajeTrio).

   Reparto (espejo de domain/Presentacion.kt de la app Android):
   - Hijo: null (participa en todos los vínculos, no se le atribuye ninguno).
   - Padre: afinidad hijo-padre + la mitad de la afinidad padre-padre.
   - Abuelo: afinidad del trío hijo-padre-abuelo (0 si es la corredora).
   Slots vacíos o árbol sin hijo: null. */

import type { AffinityModel } from './affinity'
import { SLOTS, type Seleccion } from './herencia'

export function puntosFilaPresentacion(
  modelo: AffinityModel,
  seleccion: Seleccion,
): (number | null)[] {
  const hijo = seleccion[0] ?? null
  if (hijo === null) return Array<number | null>(SLOTS).fill(null)

  const padre1 = seleccion[1] ?? null
  const padre2 = seleccion[2] ?? null
  const mitadEntrePadres =
    padre1 !== null && padre2 !== null ? modelo.puntajePar(padre1, padre2) / 2 : 0

  return Array.from({ length: SLOTS }, (_, slot) => {
    const id = seleccion[slot] ?? null
    if (id === null || slot === 0) return null
    if (slot === 1 || slot === 2) return modelo.puntajePar(hijo, id) + mitadEntrePadres
    const padre = seleccion[1 + Math.floor((slot - 3) / 2)] ?? null
    if (padre === null) return null
    return id === hijo ? 0 : modelo.puntajeTrio(hijo, padre, id)
  })
}

/* Formato local de los puntos (puede haber medio punto en los padres). */
export function formatearPuntos(valor: number, codigo: string): string {
  return new Intl.NumberFormat(codigo, { maximumFractionDigits: 1 }).format(valor)
}
