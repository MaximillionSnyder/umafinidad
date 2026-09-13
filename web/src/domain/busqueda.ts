/* Búsqueda difusa de personajes para el autocompletado del buscador.
   Porte 1:1 de domain/Busqueda.kt, sin java.text.Normalizer (se usa
   String.normalize NFD + eliminación de marcas diacríticas). */

import type { Character } from './models'

export function normalizarTexto(v: string): string {
  return v
    .normalize('NFD')
    .replace(/\p{Mn}+/gu, '')
    .replace(/[-_.']/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .toLowerCase()
}

/* Distancia de Damerau-Levenshtein (incluye transposición de adyacentes).
   Implementación clásica de tres filas, O(n·m), idéntica a la de Kotlin. */
export function distanciaDamerau(a: string, b: string): number {
  if (a === b) return 0
  const n = a.length
  const m = b.length
  if (n === 0) return m
  if (m === 0) return n

  let prevPrev = new Int32Array(m + 1)
  let prev = new Int32Array(m + 1)
  for (let j = 0; j <= m; j++) prev[j] = j
  let curr = new Int32Array(m + 1)

  for (let i = 1; i <= n; i++) {
    curr[0] = i
    const ca = a[i - 1]
    for (let j = 1; j <= m; j++) {
      const costo = ca === b[j - 1] ? 0 : 1
      let v = Math.min(
        curr[j - 1] + 1, // inserción
        prev[j] + 1, // omisión
        prev[j - 1] + costo, // sustitución o igual
      )
      if (i > 1 && j > 1 && ca === b[j - 2] && a[i - 2] === b[j - 1]) {
        v = Math.min(v, prevPrev[j - 2] + 1) // transposición
      }
      curr[j] = v
    }
    const tmp = prevPrev
    prevPrev = prev
    prev = curr
    curr = tmp
  }
  return prev[m]
}

/* Umbral adaptativo: los textos cortos exigen menos errores para no
   sugerir basura. */
export function umbralFuzzy(largoQuery: number): number {
  if (largoQuery <= 3) return 1
  if (largoQuery <= 6) return 2
  return 3
}

/* Comparación de strings por orden natural (code units), como Kotlin. */
function compararTexto(a: string, b: string): number {
  return a < b ? -1 : a > b ? 1 : 0
}

/* Puntaje de la query contra un nombre ya normalizado.
   Menor es mejor; null significa descartado. */
export function puntajeCandidato(nombreNorm: string, q: string): number | null {
  if (q.length === 0) return null
  const idx = nombreNorm.indexOf(q)
  if (idx === 0) return 0 // empieza igual que la query
  if (nombreNorm.split(' ').some((palabra) => palabra.startsWith(q))) return 100 // alguna palabra empieza igual
  if (idx > 0) return 200 + Math.min(idx, 50) // lo contiene
  const umbral = umbralFuzzy(q.length)
  let mejorDist = distanciaDamerau(q, nombreNorm)
  for (const palabra of nombreNorm.split(' ')) {
    if (Math.abs(palabra.length - q.length) <= umbral) {
      mejorDist = Math.min(mejorDist, distanciaDamerau(q, palabra))
    }
    if (mejorDist === 0) break
  }
  return mejorDist >= 1 && mejorDist <= umbral ? 300 + mejorDist * 10 : null
}

function puntajePersonaje(c: Character, q: string): number | null {
  let mejor: number | null = null
  for (const nombre of [c.enName, c.jpName, c.urlName]) {
    if (nombre === null) continue
    const p = puntajeCandidato(normalizarTexto(nombre), q)
    if (p !== null && (mejor === null || p < mejor)) mejor = p
  }
  return mejor
}

/* Top N personajes para el dropdown de sugerencias, ordenados por calidad. */
export function rankearSugerencias(personajes: Character[], query: string, limite = 5): Character[] {
  const q = normalizarTexto(query)
  if (q.length < 2) return []
  const puntuados: { char: Character; puntaje: number }[] = []
  for (const c of personajes) {
    const p = puntajePersonaje(c, q)
    if (p !== null) puntuados.push({ char: c, puntaje: p })
  }
  puntuados.sort((x, y) => x.puntaje - y.puntaje || compararTexto(x.char.enName ?? '', y.char.enName ?? ''))
  return puntuados.slice(0, limite).map((x) => x.char)
}

/* Filtro difuso para la grilla: mismo criterio que las sugerencias pero
   sin límite ni reordenamiento. */
export function coincideDifuso(c: Character, query: string): boolean {
  const q = normalizarTexto(query)
  if (q.length === 0) return true
  return puntajePersonaje(c, q) !== null
}
