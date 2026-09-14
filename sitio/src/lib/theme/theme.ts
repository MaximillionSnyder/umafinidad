/* Tokens de datos: colores de rangos, genealogía, medallas y aptitudes.
   Espejo de la app Android y del port React. */

import type { ThemeMode } from '../data/prefs'

export const RankGreat = '#4c9a5c'
export const RankGood = '#b8860b'
export const RankFair = '#c25e5e'

export function colorDeRango(clase: string | null | undefined): string | null {
  if (clase === 'rank-great') return RankGreat
  if (clase === 'rank-good') return RankGood
  if (clase === 'rank-fair') return RankFair
  return null
}

export function fondoDeRango(clase: string | null | undefined): string | null {
  const color = colorDeRango(clase)
  return color === null ? null : `color-mix(in srgb, ${color} 12%, transparent)`
}

/* Colores por genealogía de los slots de herencia:
   hijo naranja; rama del Padre 1 azul (slots 1,3,4); rama del Padre 2
   verde (slots 2,5,6). */
export const RolHijo = '#d97706'
export const GenealogiaRama1 = '#3b6ea5'
export const GenealogiaRama2 = '#4c9a5c'

export function colorDeGenealogia(slot: number): string {
  if (slot === 0) return RolHijo
  if (slot === 1 || slot === 3 || slot === 4) return GenealogiaRama1
  return GenealogiaRama2
}

/* Medallas del top de linajes. */
export const MedalOro = '#e0a800'
export const MedalPlata = '#a8b0b5'
export const MedalBronce = '#b07b4f'

export function colorDeMedalla(pos: number): string | null {
  if (pos === 0) return MedalOro
  if (pos === 1) return MedalPlata
  if (pos === 2) return MedalBronce
  return null
}

/* Escala fija de color por letra (A la mejor → G la peor). */
export function colorDeLetra(letra: string): string {
  switch (letra) {
    case 'A':
      return '#2e7d43'
    case 'B':
      return '#5c9b45'
    case 'C':
      return '#a58a1a'
    case 'D':
      return '#c47b1e'
    case 'E':
      return '#c25e2e'
    case 'F':
      return '#b3261e'
    default:
      return '#7a7f84'
  }
}

/* Mismo color base que la PWA: hsl((id × 137.508) % 360 55% 45%). */
export function colorDeAvatar(id: number): string {
  const hue = (id * 137.508) % 360
  return `hsl(${hue} 55% 45%)`
}

export function gradienteDeAvatar(id: number): string {
  const hue = (id * 137.508) % 360
  return `radial-gradient(circle at 35% 30%, hsl(${hue} 60% 58%), hsl(${hue} 55% 32%))`
}

export function inicialesDe(nombre: string): string {
  return nombre
    .split(/\s+/)
    .map((palabra) => palabra[0])
    .filter((letra): letra is string => letra !== undefined)
    .slice(0, 2)
    .join('')
    .toUpperCase()
}

export type TemaResuelto = 'dark' | 'light' | 'contrast'

export function resolverTema(tema: ThemeMode, sistemaOscuro: boolean): TemaResuelto {
  if (tema === 'ALTO_CONTRASTE') return 'contrast'
  if (tema === 'CLARO') return 'light'
  if (tema === 'OSCURO') return 'dark'
  return sistemaOscuro ? 'dark' : 'light'
}
