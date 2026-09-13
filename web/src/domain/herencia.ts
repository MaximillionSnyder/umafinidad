/* Porte 1:1 de domain/Herencia.kt (que a su vez porta src/herencia.js de
   la PWA). 7 slots fijos: [0] hijo, [1..2] padres, [3..6] abuelos
   (2 por rama). */

export const SLOTS = 7

/* Selección de 7 posiciones; null = slot vacío. */
export type Seleccion = (number | null)[]

export interface Arbol {
  hijo: number | null
  padres: (number | null)[]
  abuelosDe: (number | null)[][]
}

export function armarArbol(seleccion: Seleccion): Arbol {
  return {
    hijo: seleccion[0],
    padres: [seleccion[1], seleccion[2]],
    abuelosDe: [
      [seleccion[3], seleccion[4]],
      [seleccion[5], seleccion[6]],
    ],
  }
}

export enum Rol {
  HIJO = 'hijo',
  PADRE = 'padre',
  ABUELO = 'abuelo',
}

export function rolDeSlot(i: number): Rol {
  if (i === 0) return Rol.HIJO
  if (i <= 2) return Rol.PADRE
  return Rol.ABUELO
}

/* Reglas del juego al colocar un personaje en un slot:
   - el hijo no puede ser padre (en ninguna rama);
   - los padres deben ser distintos entre sí;
   - nadie puede ser abuelo de su propia rama;
   - los dos abuelos de una misma rama no se repiten;
   - el hijo SÍ puede ser abuelo (corredora: esa relación vale 0) y los
     cruces entre ramas están permitidos. */
export function puedeIrEn(seleccion: Seleccion, slot: number, id: number | null): boolean {
  if (slot < 0 || slot >= SLOTS || id === null) return false
  switch (rolDeSlot(slot)) {
    case Rol.HIJO:
      return seleccion[1] !== id && seleccion[2] !== id
    case Rol.PADRE: {
      const rama = slot - 1
      for (let i = 0; i <= 2; i++) {
        if (seleccion[i] === id) return false /* ya es el hijo u otro padre */
      }
      const abuelosRama = [seleccion[3 + rama * 2], seleccion[4 + rama * 2]]
      return abuelosRama.every((a) => a !== id) /* prohibido: abuelo de su propia rama */
    }
    case Rol.ABUELO: {
      const rama = Math.floor((slot - 3) / 2) /* slots 3-4 → rama 0, 5-6 → rama 1 */
      const hermano = slot % 2 === 1 ? slot + 1 : slot - 1
      if (seleccion[1 + rama] === id) return false /* su propio padre */
      return seleccion[hermano] !== id /* abuelo repetido en la rama */
    }
  }
}

/* Primer slot vacío donde el personaje sí pueda ir. Devuelve:
   índice del slot libre, -1 si la selección está completa,
   -2 si hay huecos pero ninguno le sirve al personaje. */
export function slotPara(seleccion: Seleccion, id: number): number {
  const hayHueco = seleccion.some((v) => v === null)
  for (let i = 0; i < SLOTS; i++) {
    if (seleccion[i] === null && puedeIrEn(seleccion, i, id)) return i
  }
  return hayHueco ? -2 : -1
}

export enum TipoVinculo {
  HIJO_PADRE = 'hijo-padre',
  ENTRE_PADRES = 'entre-padres',
  HIJO_PADRE_ABUELO = 'hijo-padre-abuelo',
}

export interface Vinculo {
  tipo: TipoVinculo
  ids: number[]
  esCorredora: boolean
}

export function vinculos(arbol: Arbol): Vinculo[] {
  const v: Vinculo[] = []
  const { hijo, padres, abuelosDe } = arbol
  if (hijo !== null) {
    for (const p of padres) {
      if (p !== null) v.push({ tipo: TipoVinculo.HIJO_PADRE, ids: [hijo, p], esCorredora: false })
    }
  }
  if (padres[0] !== null && padres[1] !== null) {
    v.push({ tipo: TipoVinculo.ENTRE_PADRES, ids: [padres[0], padres[1]], esCorredora: false })
  }
  /* Regla del juego: cada abuelo se vincula con el padre de su misma rama
     y el hijo a la vez. Si el abuelo es la misma corredora, vale 0. */
  if (hijo !== null) {
    for (let p = 0; p <= 1; p++) {
      const padre = padres[p]
      if (padre === null) continue
      for (const a of abuelosDe[p]) {
        if (a !== null) {
          v.push({
            tipo: TipoVinculo.HIJO_PADRE_ABUELO,
            ids: [hijo, padre, a],
            esCorredora: a === hijo,
          })
        }
      }
    }
  }
  return v
}

export function posicionesDe(seleccion: Seleccion, id: number): number[] {
  const out: number[] = []
  seleccion.forEach((v, i) => {
    if (v === id) out.push(i)
  })
  return out
}

export function contarSeleccionados(seleccion: Seleccion): number {
  return seleccion.filter((v) => v !== null).length
}
